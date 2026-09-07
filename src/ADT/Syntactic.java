package ADT;

import java.util.List;
import java.util.ArrayList;

/**
 * @author lmorsman
 */
public class Syntactic {

    private String filein;              //The full file path to input file
    private SymbolTable symbolList;     // Symbol table storing ident/const
    private DataMem mem;				// DataMem table for memory storage
    private QuadTable quads;			// QuadTable for code generation
    private Lexical lex;                // Lexical analyzer 
    private Lexical.token token;        // Next Token retrieved
    private Interpreter interp;			// interpreter to execute quads
    private boolean traceon;            // Controls tracing mode 
    private int level = 0;              // Controls indent for trace mode
    private boolean anyErrors;          // Set TRUE if an error happens 
    private boolean errorsFound;		// Set TRUE if an error happens, but NEVER set back to false. This allows end of program reporting that an error was encountered
    private boolean finishDeclarations; // Set TRUE when variabledecsec concludes to indicate no more vars should be declared. Used for error handling
    private List<Integer> backfillQuad;		// This is a running list of forward quad lookups that need to be backfilled at some point
    private List<String> backfillSymbol; 	// Supports the running list of quad lookups by matching a symbol to the quad we will backfill later
    private final int symbolSize = 250;
    private final int memSize = 500;
    private final int quadsSize = 500;
    
    // opcode constants
 	private final int STOP_OPCODE = 0;
 	private final int DIV_OPCODE = 1;
 	private final int MUL_OPCODE = 2;
 	private final int SUB_OPCODE = 3;
 	private final int ADD_OPCODE = 4;
 	private final int MOV_OPCODE = 5;
 	private final int PRINT_OPCODE = 6;
 	private final int READ_OPCODE = 7;
 	private final int JMP_OPCODE = 8;
 	private final int JZ_OPCODE = 9;
 	private final int JP_OPCODE = 10;
 	private final int JN_OPCODE = 11;
 	private final int JNZ_OPCODE = 12;
 	private final int JNP_OPCODE = 13;
 	private final int JNN_OPCODE = 14;
 	private final int JINDR_OPCODE = 15;
 	
 	// symbol table constants
 	private final int SYMBOLINDEX_ZERO = 0;
 	private final int SYMBOLINDEX_PLUSONE = 1;
 	private final int SYMBOLINDEX_MINUSONE = 2;
 	
 	// relop token code constants
 	private final int GT_TOKENCODE = 38;
 	private final int LT_TOKENCODE = 39;
 	private final int GTEQ_TOKENCODE = 40;
 	private final int LTEQ_TOKENCODE = 41;
 	private final int EQ_TOKENCODE = 42;
 	private final int NEQ_TOKENCODE = 43;

    // Constructor
    public Syntactic(String filename, boolean traceOn) {
        filein = filename;
        traceon = traceOn;
        symbolList = new SymbolTable(symbolSize);
        mem = new DataMem(memSize);
        quads = new QuadTable(quadsSize);
        lex = new Lexical(filein, symbolList, true);
        lex.setPrintToken(traceOn);
        interp = new Interpreter();
        anyErrors = false;
        errorsFound = false;
        finishDeclarations = false;
        backfillQuad = new ArrayList<Integer>();
        backfillSymbol = new ArrayList<String>();
    } // Constructor

    
    
	// The interface to the syntax analyzer, initiates parsing
	// Uses variable RECUR to get return values throughout the non-terminal methods    
    public void parse() {

        int recur = 0;
        String filenameBase = filein.substring(0, filein.length() - 4);
        // prime the pump to get the first token to process
        token = lex.GetNextToken();
        // init the -1 and 1 constants in datamem
        mem.Malloc(-1);
        mem.Malloc(1);
        // call PROGRAM
        recur = Program();
        quads.AddQuad(STOP_OPCODE, 0, 0, 0);
        
        // print ST, QUAD, MEM before execute
        symbolList.PrintSymbolTable(filenameBase + "SymbolTable.txt");
        quads.PrintQuadTable(filenameBase + "QUADS.txt");
        mem.PrintDataMem(filenameBase + "MEMORYBefore.txt");
        
        if(errorsFound) {
        	System.out.println("Errors, unable to run program.");
        } else {
        	// interpret & execute
        	interp.InterpretQuads(quads, mem, traceon, filenameBase + "TRACE.txt");
        	mem.PrintDataMem(filenameBase + "MEMORYAfter.txt");
        }
    } // parse
    
    // Some statements may still be actionable even in an error state. This allows the parser to continue analyzing statements through an error
    private int ErrorRecovery() {
    	
    	int recur = 0;
    	trace("ErrorRecovery", true);
    	
    	while (token.code != lex.codeFor("DOT_")) {
    		
    		// consume the token that caused an error and move on
    		token = lex.GetNextToken(); 
    		System.out.println("** Resynch found: " + token.lexeme + ".");
    		
    		// end of file not yet found
    		if(token.code != lex.codeFor("DOT_")) {
    			
    			// if next token is a statement start
    			if(token.code == lex.codeFor("IDNT") || token.code == lex.codeFor("BGIN") || token.code == lex.codeFor("IF__") || token.code == lex.codeFor("WHIL") || token.code == lex.codeFor("FOR_") || token.code == lex.codeFor("REPT") || token.code == lex.codeFor("GOTO") || token.code == lex.codeFor("RDLN") || token.code == lex.codeFor("WRLN")) {
    				// ignore the current error in the next statement, and attempt to read the statement
    				anyErrors = false; 
    				recur = Statement();
    			}
    			
    		}
    		
    	}
    	
    	trace("ErrorRecovery", false);
    	return recur;
    	
    } // ErrorRecovery

    // Non Terminal PROGIDENTIFIER is fully implemented here, leave it as-is.        
    private int ProgIdentifier() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("ProgIdentifier", true);

        // This non-term is used to uniquely mark the program identifier
        if (token.code == lex.codeFor("IDNT")) {
            // Because this is the progIdentifier, it will get a 'PROGRAM' type to prevent re-use as a var
            symbolList.SetPurpose(symbolList.LookupSymbol(token.lexeme), SymbolPurpose.PROGRAM);
            symbolList.SetType(symbolList.LookupSymbol(token.lexeme), DataType.PROGRAMTYPE);
            //move on
            token = lex.GetNextToken();
        }
        trace("ProgIdentifier", false);
        return recur;
    } // ProgIdentifier

    // Non Terminal PROGRAM is fully implemented here.    
    private int Program() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Program", true);
        // program start found
        if (token.code == lex.codeFor("PROG")) {
            token = lex.GetNextToken();
            recur = ProgIdentifier();
            if (token.code == lex.codeFor("SMCL")) {
                token = lex.GetNextToken();
                recur = Block();
                
                // if the block encountered an error, enter error recovery
                if(anyErrors) {
                	recur = ErrorRecovery();
                }
                
                if (token.code == lex.codeFor("DOT_")) {
                    if (!anyErrors && !errorsFound) {
                        System.out.println("Success.");
                    } else {
                        System.out.println("Compilation failed.");
                    }
                } else {
                    error(lex.reserveFor("DOT_"), token.lexeme);
                }
            } else {
                error(lex.reserveFor("SMCL"), token.lexeme);
            }
        } else {
        	// program start not found
            error(lex.reserveFor("PROG"), token.lexeme);
        }
        trace("Program", false);
        return recur;
    } // Program

    // Non Terminal BLOCK is fully implemented here.    
    private int Block() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Block", true);
        recur = Variabledecsec();
        recur = Blockbody();
        trace("Block", false);
        return recur;
    } // Block
    
    // used for list of statements in a begin/end block of statements
    private int Statementlist(boolean repeating) {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Statementlist", true);
        
        // recurse into first statement body
        recur = Statement();
        
        // keep reading statements until no more semi colons are found or an error has occurred
        while(token.code == lex.codeFor("SMCL") && !anyErrors) {
        	// increment to next token
        	token = lex.GetNextToken();
        	if(!repeating || token.code != lex.codeFor("UNTL")) {
        		// recurse into next statement body
	        	recur = Statement();
        	}
        }
        
        trace("Statementlist", false);
        return recur;
    } // Statementlist
    
    /*
     * Here is the rough CFG for how the variable delcaration section is handled
     * Variabledecsec => VariableDeclaration Variabledecsec | sigma
	 * VariableDeclaration => Identifierlist : SimpleType
	 * Identifierlist => , identifier identifierlist | sigma
	 * SimpleType => Integer | Float | String | Label
     */
    
    // used for handling the beginning of variable declarations
    // Variabledecsec => VariableDeclaration Variabledecsec | sigma
    private int Variabledecsec() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Variabledecsec", true);
        
        // check if current token is a var. if so begin/continue variable identification
        while(token.code == lex.codeFor("IDNT") &&  !anyErrors) {
        	
        	// begin variable recursion
        	recur = VariableDeclaration();
        	
        	// increment to next token
        	token = lex.GetNextToken();
        	
        }
        
        // mark the end of the variable declaration section
        symbolList.SetFinishDeclarations();
        
        trace("variabledecsec", false);
        return recur;
    } // Variabledecsec
    
    // handle variable declarations
    // VariableDeclaration => Identifierlist : SimpleType
    private int VariableDeclaration() {
    	int recur = 0;
    	if (anyErrors) {
    		return -1;
    	}
    	
    	trace("VariableDeclaration", true);
    	
    	// get the full list of variable names for this line
    	List<String> tokenList = Identifierlist();
    	
    	// variable names list has ended, check for colon
    	if(token.code == lex.codeFor("COLN")) {
    		
    		token = lex.GetNextToken(); // increment to next token
    		recur = SimpleType(); // check that a primitive was selected
    		
    		for(int i = 0; i < tokenList.size(); i++) {
	    		int symbolIndex = symbolList.LookupSymbol(tokenList.get(i));
	    		symbolList.PutAddress(symbolIndex, AllocateMem(symbolList.GetType(symbolIndex)));
    		}
    		
    	} else {
    		error(lex.reserveFor("COLN"), token.lexeme);
    	}
    	
    	trace("VariableDeclaration", false);
    	return recur;
    } // VariableDeclaration
    
    // handles lists of variables per declaration in a line, comma-separated
    // Identifierlist => , identifier identifierlist | sigma
    private List<String> Identifierlist() {
    	
    	// collect token lexemes to a list and return the list
    	List<String> recur = new ArrayList<>();
    	
    	if (anyErrors) {
    		return recur;
    	}
    	
    	trace("Identifierlist", true);
    	
    	// consume current token lexeme and add to list. 
    	recur.add(token.lexeme);
    	
    	// increment to next token and begin identifier declarations
    	token = lex.GetNextToken();
    	
    	// comma indicates more variable names are being declared
    	if(token.code == lex.codeFor("COMA")) {
    		
    		// increment to next token
    		token = lex.GetNextToken();
    		
    		// verify next token is an identifier for next var name
    		if(token.code == lex.codeFor("IDNT")) {
    			// recurse into identifier list and read more identifiers if more are found. Adds all token lexemes to current list
    			recur.addAll(Identifierlist());
    		} else {
    			// expected an identifier but received something else
    			error(lex.reserveFor("IDNT"), token.lexeme);
    		}
    		
    	}
    	
    	trace("Identifierlist", false);
    	return recur;
    } // Identifierlist

    // handles the primitive variable type selection
    // SimpleType => Integer | Float | String | Label
    private int SimpleType() {
    	int recur = 0;
    	if (anyErrors) {
    		return -1;
    	}
    	
    	trace("SimpleType", true);
    	
    	// verify a valid type is selected
    	if(token.code == lex.codeFor("INTG")) {
    		// integer type selected, increment to next token and exit
    		symbolList.UpdateTypes(DataType.INTEGERTYPE);
    		token = lex.GetNextToken();
    	} else if(token.code == lex.codeFor("FLOT")) {
    		// float type selected, increment to next token and exit
    		symbolList.UpdateTypes(DataType.FLOATTYPE);
    		token = lex.GetNextToken();
    	} else if(token.code == lex.codeFor("STRN")) {
    		// string type selected, increment to next token and exit
    		symbolList.UpdateTypes(DataType.STRINGTYPE);
    		token = lex.GetNextToken();
    	} else if(token.code == lex.codeFor("LABL")) {
    		// label type selected, increment to next token and exit
    		symbolList.UpdateTypes(DataType.LABELTYPE);
    		token = lex.GetNextToken();
    	} else {
    		// expected a variable primitive, but one was not read
    		error(lex.reserveFor("INTG") + " / " + lex.reserveFor("FLOT") + " / " + lex.reserveFor("STRN") + " / " + lex.reserveFor("LABL"), token.lexeme);
    	}
    	
    	trace("SimpleType", false);
    	return recur;
    } // SimpleType
    
    private int Blockbody() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("Blockbody", true);

        // start of block body
        if (token.code == lex.codeFor("BGIN")) {
            token = lex.GetNextToken(); 
            recur = Statementlist(false); // recurse into block body
            // end of block body
            if (token.code == lex.codeFor("END_")) {
                token = lex.GetNextToken();
            } else {
            	// no ending for block body was found
                error(lex.reserveFor("END_"), token.lexeme);
            }

        } else {
        	// no start point for block body was found
            error(lex.reserveFor("BGIN"), token.lexeme);
        }

        trace("Blockbody", false);
        return recur;
    } // Blockbody
    
    // <label> $COLON 
    private int handleLabel() {
    	int recur = 0;
    	if(anyErrors) {
    		return -1;
    	}
    	
    	trace("handleLabel", true);
    	
    	// set the label address to the next quad location for jumps
    	if(backfillSymbol.contains(token.lexeme)) {
    		// this is a token that we designated as needing a backfill. Backfill the symbol with what was determined to be the next quad at the last time it was marked for backfill
    		int backfillItemIndex = backfillSymbol.indexOf(token.lexeme);
    		quads.UpdateJump(backfillQuad.get(backfillItemIndex), quads.NextQuad());
    		backfillSymbol.remove(backfillItemIndex); // clean out backfill item
    		backfillQuad.remove(backfillItemIndex); // clean out backfill item
    	} else {
    		symbolList.PutAddress(symbolList.LookupSymbol(token.lexeme), quads.NextQuad());
    	}
    	token = lex.GetNextToken();
    	if (token.code == lex.codeFor("COLN")) {
    		token = lex.GetNextToken();
    	} else {
    		error(lex.reserveFor("COLN") + " after LABEL", token.lexeme);
    	}
    	
    	trace("handleLabel", false);
    	
    	return recur;
    } // handleLabel

    //Not a Non Terminal, but used to shorten Statement code body for readability.   
    //<variable> $COLON-EQUALS <simple expression>
    private int handleAssignment() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleAssignment", true);
        //have ident already in order to get to here, handle as Variable
        int left = Variable();  //Variable moves ahead, next token ready, store assignment var for quad gen

        if (token.code == lex.codeFor("CLEQ")) {
            token = lex.GetNextToken();
            int right = SimpleExpression();
            quads.AddQuad(MOV_OPCODE, right, 0, left); // generate assignment quad
        } else {
            error(lex.reserveFor("CLEQ"), token.lexeme);
        }

        trace("handleAssignment", false);
        return recur;
    } // handleAssignment
    
    /*
     * $IF -> <relexpression> $THEN <statement> [$ELSE <statement>]
     */
    private int handleIf() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleIf", true);
        
        // increment to next token
        token = lex.GetNextToken();
        int branchQuad = Relexpression(); // tells where branchtarget to be set for jumping around TRUE part
        int patchElse; // later set for saving backfill quads
        
        // if must always be followed by then
        if (token.code == lex.codeFor("THEN")) {
        	// statement recursion
        	token = lex.GetNextToken();
        	recur = Statement();
        	if(token.code == lex.codeFor("ELSE")) {
        		// token for else condition was found
        		token = lex.GetNextToken();
        		patchElse = quads.NextQuad(); // save backfill quad to jump around ELSE body, target is not yet known
        		quads.AddQuad(JMP_OPCODE, 0, 0, 0); // conditional jump
        		quads.UpdateJump(branchQuad,  quads.NextQuad()); // conditional jump
        		recur = Statement(); // recurse into statement body
        		quads.UpdateJump(patchElse, quads.NextQuad()); // backfill quad to jump around else body
        	} else {
        		// no ELSE was encountered. Fix jumps for IF
        		quads.UpdateJump(branchQuad, quads.NextQuad());
        	}
        } else {
        	// expected THEN but received something else
        	error(lex.reserveFor("THEN"), token.lexeme);
        }
        
        trace("handleIf", false);
    	return recur;
    } // handleIf
    
    /*
     * $FOR -> <variable> $ASSIGN <simple expression> $TO <simple expression> $DO <statement>
     */
    private int handleFor() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        
        trace("handleFor", true);
        
        // increment to next token and recurse into variable
        token = lex.GetNextToken();
        int loop = Variable(); // this is our loop variable
        
        // verify var assignment token
        if(token.code == lex.codeFor("CLEQ")) {
        	
        	// increment to next token and recurse into SimpleExpression
        	token = lex.GetNextToken();
        	int startFor = SimpleExpression(); // start var of the for loop
        	quads.AddQuad(MOV_OPCODE, startFor, 0, loop); // set the starting loop variable value
        	if(token.code == lex.codeFor("TO__")) {
        		
        		// increment to next token and recurse into SimpleExpression
        		token = lex.GetNextToken();
        		
        		int saveTop = quads.NextQuad(); // saving the top of loop to jump back to later
        		int exit = SimpleExpression(); // saving the end condition of the loop
        		
        		int temp = mem.Malloc(0); // storage for arithmetic handling
        		quads.AddQuad(SUB_OPCODE, loop, exit, temp); // compare the start var to the end condition for branch out
        		int branchQuad = quads.NextQuad(); // branch out location
        		quads.AddQuad(JP_OPCODE, temp, 0, 0); // jump out of loop, backfills later
        		if (token.code == lex.codeFor("DO__")) {
        			// increment to next token and recurse into statement
        			token = lex.GetNextToken();
        			recur = Statement();
        			
        			int increment = mem.Malloc(0); // temp variable for incrementation
        			quads.AddQuad(ADD_OPCODE,  loop, SYMBOLINDEX_PLUSONE, increment); // increment our loop tracker by one and store to temp
        			quads.AddQuad(MOV_OPCODE, increment, 0, loop); // move temp to loop tracker var
        			quads.AddQuad(JMP_OPCODE, 0, 0, saveTop); // unconditional jump back to top of loop where continuation comparison will occur
        			quads.UpdateJump(branchQuad, quads.NextQuad()); // backfill the branch exit now that loop quads have been generated
        		} else {
        			// expected DO token but did not receive it
        			error(lex.reserveFor("DO__"), token.lexeme);
        		}
        		
        	} else {
        		// expected TO token but did not receive it
        		error(lex.reserveFor("TO__"), token.lexeme);
        	}
        	
        } else {
        	// expected var assignment but did not receive it
        	error(lex.reserveFor("CLEQ"), token.lexeme);
        }
        
        trace("handleFor", false);
    	return recur;
    	
    } // handleFor
    
    /*
     * $REPEAT -> <statementlist> $UNTIL <relexpression>
     */
    private int handleRepeat() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleRepeat", true);
        
        // increment to next token
        token = lex.GetNextToken();
        
        int branchTarget = quads.NextQuad(); // before generating code, save branch start point
        
        // recurse into statement list
        recur = Statementlist(true);
        
        if(token.code == lex.codeFor("UNTL")) {
        	token = lex.GetNextToken(); // increment to next token
        	int branchQuad = Relexpression(); // tells where branchTarget is set
        	quads.UpdateJump(branchQuad, branchTarget); // set where to go
        } else {
        	// expected "UNTIL" but received something else
        	error(lex.reserveFor("UNTL"), token.lexeme);
        }
        
        trace("handleRepeat", false);
    	return recur;
    } // handleRepeat
    
    /*
     * $WHILE -> <relexpression> DO <statement>
     */
    private int handleWhile() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleWhile", true);
        
        // increment to next token and recurse into relational expression 
        token = lex.GetNextToken();
        int saveTop = quads.NextQuad(); // store the start point
        int branchQuad = Relexpression(); // store the expression quad position
        
        // handle loop repeats
        if(token.code == lex.codeFor("DO__")) {
        	token = lex.GetNextToken(); // increment to next token
        	recur = Statement(); // recurse into statement
        	quads.AddQuad(JMP_OPCODE, 0, 0, saveTop); // jump to the top of the loop
        	quads.UpdateJump(branchQuad, quads.NextQuad()); // conditional jump to next quad out of loop
        } else {
        	// expected DO but received something else
        	error(lex.reserveFor("DO__"), token.lexeme);
        }
        
        trace("handleWhile", false);
    	return recur;
    } // handleWhile
    
    /*
     * $GOTO -> <labelidentifier>
     */
    private int handleGoto() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleGoto", true);
        
        // increment to next token
        token = lex.GetNextToken();
        
        // labelIdentifier
        if((token.code == lex.codeFor("IDNT")) && (symbolList.GetPurpose(symbolList.LookupSymbol(token.lexeme)) == SymbolPurpose.LABEL)) {
        	int address = symbolList.GetAddress(symbolList.LookupSymbol(token.lexeme)); // get the address for the jump. This address is not a datamem address, but rather the quad index of the jump
        	if(address == -1) {
        		// if we are attempting to jump to a quad that has not yet been initialized. This is a forward jump, we need to backfill this later
        		backfillQuad.add(quads.NextQuad()); // store backfill quad number to list
        		backfillSymbol.add(token.lexeme); // store backfill token lexeme to list
        	}
        	quads.AddQuad(JINDR_OPCODE, 0, 0, symbolList.GetAddress(symbolList.LookupSymbol(token.lexeme))); // indirect jump to target label
        	token = lex.GetNextToken(); // increment to next token and move on
        } else if ((token.code == lex.codeFor("IDNT")) && (symbolList.GetPurpose(symbolList.LookupSymbol(token.lexeme)) == SymbolPurpose.VARIABLE)) {
        	// goto requires a label for jumps, but the next identifier found was a variable
        	System.out.println("ERROR: " + token.lexeme + " is being used as a LABEL, but is a VARIABLE. Continuing...");
        	errorsFound = true;
        } else if ((token.code == lex.codeFor("IDNT")) && (symbolList.GetPurpose(symbolList.LookupSymbol(token.lexeme)) == SymbolPurpose.PROGRAM)) {
        	// goto requires a label for jumps, but the next identifier found was a program
        	System.out.println("ERROR: " + token.lexeme + " is being used as a LABEL, but is a PROGRAM. Continuing...");
        	errorsFound = true;
        } else {
        	// expected a label identifier but received something else
        	error(lex.reserveFor("LABL"), token.lexeme);
        }
        
        trace("handleGoto", false);
    	return recur;
    } // handleGoto
    
    /*
     * $READLN -> $LPAR <identifier> $RPAR
     */
    private int handleReadln() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleReadln", true);
        
        // increment to next token 
        token = lex.GetNextToken();
        
        if(token.code == lex.codeFor("LPTS")) {
        	
        	// increment to next token
        	token = lex.GetNextToken();
        	
        	// expect an identifier following (
        	if(token.code == lex.codeFor("IDNT")) {
        		
        		// code gen for reading a line
        		quads.AddQuad(READ_OPCODE, 0, 0, symbolList.GetAddress(symbolList.LookupSymbol(token.lexeme)));
        		
        		// bookkeeping and move on
        		token = lex.GetNextToken();

    			// end of READLN statement
        		if(token.code == lex.codeFor("RPTS")) {
        			token = lex.GetNextToken();
        		} else {
        			// expected an identifier but received something else
        			error(lex.reserveFor("RPTS"), token.lexeme);
        		}
        		
        	} else {
        		// expected an identifier but received something else
        		error(lex.reserveFor("IDNT"), token.lexeme);
        	}
        	
        } else {
        	// expected left parenthesis '(' but received something else
        	error(lex.reserveFor("LPTS"), token.lexeme);
        }
        
        trace("handleReadln", false);
    	return recur;
    } // handleReadln
    
    /*
     * $WRITELN -> $LPAR (<simple expression> | <identifier> | <stringconst> ) $RPAR|
     */
    private int handleWriteln() {
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        trace("handleWriteln", true);
        
        token = lex.GetNextToken();
        
        if(token.code == lex.codeFor("LPTS")) {
        	
        	// increment to next token
        	token = lex.GetNextToken();
        	
        	/*if(token.code == lex.codeFor("IDNT")) {
        		
        		// increment to next token
        		quads.AddQuad(PRINT_OPCODE, 0, 0, symbolList.GetAddress(symbolList.LookupSymbol(token.lexeme))); // write the identifier that was found, fetch its address from the symboltable
        		token = lex.GetNextToken();
        		
        		// verify writeln statement closure
        		if(token.code == lex.codeFor("RPTS")) {
        			// bookkeeping and move on
        			token = lex.GetNextToken();
        		} else {
        			// expected closing parenthesis token but received something else
        			error(lex.reserveFor("RPTS"), token.lexeme);
        		}
        		
        	} else*/ if (token.code == lex.codeFor("STRL")) {
        		
        		// recurse into unsigned string for literal handling
        		recur = UnsignedString();
        		quads.AddQuad(PRINT_OPCODE, 0, 0, recur); // write the string literal, fetch its address from the symboltable
        		
        		// verify writeln statement closure
        		if(token.code == lex.codeFor("RPTS")) {
        			// bookkeeping and move on
        			token = lex.GetNextToken();
        		} else {
        			// expected closing parenthesis token but received something else
        			error(lex.reserveFor("RPTS"), token.lexeme);
        		}
        		
        	} else {
        		// the only remaining option that could be in writeln is a simpleexpression
        		
        		
        		//token = lex.GetNextToken(); // increment to next token
        		recur = SimpleExpression(); // recurse into SimpleExpression
        		quads.AddQuad(PRINT_OPCODE, 0, 0, recur); // write whatever is at the address of the last simple expression
        		
        		// verify writeln statement closure
        		if(token.code == lex.codeFor("RPTS")) {
        			// bookkeeping and move on
        			token = lex.GetNextToken();
        		} else {
        			// expected closing parenthesis token but received something else
        			error(lex.reserveFor("RPTS"), token.lexeme);
        		}
        		
        	}
        	
        } else {
        	// expected a left parenthesis '(' but received something else 
        	error(lex.reserveFor("LPTS"), token.lexeme);
        }
        
        trace("handleWriteln", false);
        
    	return recur;
    } // handleWriteln()
    
    /*
     * <relexpression> -> <simple expression> <relop> <simple expression>
     */
    private int Relexpression() {
    	
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        
        int left; // left operand
        int right; // right operand
        int saveRelop; // relational operator var
        int temp; // temporary arithmetic data storage
        
        trace("Relexpression", true);
        
        // Calls always occur in the same order, and token checks happen in those respective functions so Relexpression exists just to enforce this workflow
        left = SimpleExpression(); // get the left operand
        saveRelop = Relop(); // returns tokencode of rel
        right = SimpleExpression(); // get the right operand
        temp = mem.Malloc(0); // address of result var
        quads.AddQuad(SUB_OPCODE, left, right, temp); // all conditional operations can be broken down to subtraction. Compare left - right
        recur = quads.NextQuad(); // save Quad index where branch will be
        quads.AddQuad(relopToOpcode(saveRelop), temp, 0, 0); // target is backfilled later, just adds the correct relational opcode to the table
        
        trace("Relexpression", false);
        
    	return recur;
    } // Relexpression
    
    // relop is the token found. Jump logic lookup table
    private int relopToOpcode(int relop) {
    	
    	int result;
    	
    	switch(relop) {
    		case EQ_TOKENCODE:
    			result = JNZ_OPCODE;
    			break;
    		case NEQ_TOKENCODE:
    			result = JZ_OPCODE;
    			break;
    		case LT_TOKENCODE:
    			result = JNN_OPCODE;
    			break;
    		case GT_TOKENCODE:
    			result = JNP_OPCODE;
    			break;
    		case LTEQ_TOKENCODE:
    			result = JP_OPCODE;
    			break;
    		case GTEQ_TOKENCODE:
    			result = JN_OPCODE;
    			break;
    		default:
    			result = JINDR_OPCODE;
    	}
    	
    	return result;
    } // relopToOpcode
    
    /*
     * <relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
     */
    private int Relop() {
    	
    	int recur = 0;
        if (anyErrors) {
            return -1;
        }
        
        trace("Relop", true);
        
        if(token.code == lex.codeFor("EQ__")) { // token is =
        	// bookkeeping and move on
        	recur = EQ_TOKENCODE;
        	token = lex.GetNextToken();
        } else if (token.code == lex.codeFor("LT__")) { // token is <
        	// bookkeeping and move on
        	recur = LT_TOKENCODE;
        	token = lex.GetNextToken();
        } else if (token.code == lex.codeFor("GT__")) { // token is >
        	// bookkeeping and move on
        	recur = GT_TOKENCODE;
        	token = lex.GetNextToken();
        } else if (token.code == lex.codeFor("NEQ_")) { // token is <>
        	// bookkeeping and move on
        	recur = NEQ_TOKENCODE;
        	token = lex.GetNextToken();
        } else if (token.code == lex.codeFor("LTEQ")) { // token is <=
        	// bookkeeping and move on
        	recur = LTEQ_TOKENCODE;
        	token = lex.GetNextToken();
        } else if (token.code == lex.codeFor("GTEQ")) { // token >=
        	// bookkeeping and move on
        	recur = GTEQ_TOKENCODE;
        	token = lex.GetNextToken();
        } else {
        	// expected a relational operator, but received something else instead
        	error(lex.reserveFor("EQ__") + "/" + lex.reserveFor("LT__") + "/" + lex.reserveFor("GT__") + "/" + lex.reserveFor("NEQ_") + "/" + lex.reserveFor("LTEQ") + lex.reserveFor("GTEQ"), token.lexeme);
        }
        
        trace("Relop", false);
        
    	return recur;
    } // Relop
	
    // NonTerminal This is dummied in to only work for a SINGLE identifier. 
	//  It will work with the SyntaxAMiniTest file having ASSIGNMENT statements
	//     containing only IDENTIFIERS.  TERM and FACTOR and numbers will be
	//     needed to complete Part A.
	// SimpleExpression MUST BE 
	//  COMPLETED TO IMPLEMENT CFG for <simple expression>
    private int SimpleExpression() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("SimpleExpression", true);
        
        /*
         * CFG that represents how simple expressions are handled in the language
         * E => T | -T | T + E | T - E
         * T => F | F * T | F / T
         * F => a | (E)
         */
        // Local class expression to handle the CFG of the simple expression
        class LocalExpression {
        	
        	// representation of expressions
        	// E => T | -T | T + E | T - E
        	int Expression() {
        		
        		if(anyErrors) {
        			return -1;
        		}
        		
        		int recur = 0;
        		int signval = SYMBOLINDEX_PLUSONE; // assume positive sign
        		int temp; // temporary arithmetic data storage
        		
        		trace("Expression", true);
        		
        		// Check for a sign in front of first term. optional 
        		if(token.code == lex.codeFor("MNUS")) {
        			signval = Sign();
        		}
        		
        		// Term is always present in E transform, so store its address
        		int left = Term();
        		
        		// add a negation quad for the term
        		if(signval == -1) {
        			temp = mem.Malloc(0);
        			quads.AddQuad(MUL_OPCODE, left, SYMBOLINDEX_MINUSONE, temp);
        		}
        		
        		// T + E production rule
        		if(token.code == lex.codeFor("PLUS")) {
        			token = lex.GetNextToken();
        			int right = Expression(); // recurse, address of expression in result
        			temp = mem.Malloc(0); // address of result var
        			quads.AddQuad(ADD_OPCODE, left, right, temp); // addition operation
        			left = temp; // new leftmost term is last result
        		} 
        		// T - E production rule
        		else if (token.code == lex.codeFor("MNUS")){
        			token = lex.GetNextToken();
        			int right = Expression(); // recurse, address of expression in result
        			temp = mem.Malloc(0); // address of result var
        			quads.AddQuad(SUB_OPCODE, left, right, temp); // subtraction operation
        			left = temp; // new leftmost term is last result
        		}
        		
        		recur = left;
        		
        		trace("Expression", false);
        		return recur;
        		
        	} // Expression
        	
        	// representation of terms
        	// T => F | F * T | F / T
        	int Term() {
        		
        		if(anyErrors) {
        			return -1;
        		}
        		
        		int recur = 0;
        		int temp = 0; // temporary arithmetic data storage
        		
        		trace("Term", true);
        		
        		// Factor is always present in T transform, so assign it to recur
        		int left = Factor();
        		
        		// F * T production rule
        		if(token.code == lex.codeFor("MULT")) {
        			token = lex.GetNextToken();
        			int right = Term(); // recurse, address of expression in result
        			temp = mem.Malloc(0); // address of result var
        			quads.AddQuad(MUL_OPCODE, left, right, temp);
        			left = temp; // new leftmost term is last result
        		}
        		// F / T production rule
        		else if(token.code == lex.codeFor("DIVD")) {
        			token = lex.GetNextToken();
        			int right = Term(); // recurse, address of expression in result
        			temp = mem.Malloc(0); // address of result var
        			quads.AddQuad(DIV_OPCODE, left, right, temp);
        			left = temp; // new leftmost term is last result
        		}
        		
        		recur = left;
        		
        		trace("Term", false);
        		return recur;
        		
        	} // Term
        	
        	// representation of Factors
        	// F => a | (E)
        	int Factor() {
        		
        		if(anyErrors) {
        			return -1;
        		}
        		
        		trace("Factor", true);
        		
        		int recur = 0;
        		
        		// T => a production rule where a is an identifier (var) or literal
        		if(token.code == lex.codeFor("IDNT")) {
        			recur = Variable();
        		} else if(token.code == lex.codeFor("INTL") || token.code == lex.codeFor("FLTL")) {
        			// T => a production rule where a is a literal
        			recur = UnsignedConstant();
        		} else {
        			
        			// (E) production rule
            		if(token.code == lex.codeFor("LPTS")) {
            			
            			// increment to next token and recurse for next expression
            			token = lex.GetNextToken();
            			recur = Expression();
            			
            			// look for ')' to close out production rule
            			if(token.code == lex.codeFor("RPTS")) {
            				// bookkeeping and move on
            				token = lex.GetNextToken();
            			} else {
            				// ')' not found, invalid syntax
            				error(lex.reserveFor("RPTS"), token.lexeme);
            			}
            			
            		} else {
            			// '(' not found, invalid syntax. Number, '(', or variable were expected
            			error("Number / Variable / " + lex.reserveFor("LPTS"), token.lexeme);
            		}
            		
    			}
        		
        		trace("Factor", false);
        		return recur;
        		
        	} // Factor
        	
        	// Signing for terms. Only '-' is accepted at this time
        	int Sign() {
        		int recur = 1;
        		if(anyErrors) {
        			return -1;
        		}
        		
        		trace("Sign", true);
        		
        		// verify current token is a sign for a term 
        		if (token.code == lex.codeFor("MNUS")) {
                    // bookkeeping and move on
        			recur = SYMBOLINDEX_MINUSONE; // return the address of -1 from symbol table
                    token = lex.GetNextToken();
                } else {
                	// expected a sign but something else was received. 
                    error("Sign", token.lexeme);
                }
        		
        		trace("Sign", false);
        		
        		return recur;
        	} // Sign
        	
        } // LocalExpression
        
        // instantiate local class as object to build a simple expression
        LocalExpression simpExpression = new LocalExpression();
        
        // recurse into simple expression building local class
        recur = simpExpression.Expression();
        
        trace("SimpleExpression", false);
        return recur;
    } // SimpleExpression

    // Eventually this will handle all possible statement starts in 
	//    a nested if/else or switch structure. Only ASSIGNMENT is implemented now.
    private int Statement() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Statement", true);
        
        //Collecting labels which precede a statement
        while ((token.code == lex.codeFor("IDNT")) && (symbolList.GetPurpose(symbolList.LookupSymbol(token.lexeme)) == SymbolPurpose.LABEL)) {
        	recur = handleLabel();
        } // while labels found 
        
        if (token.code == lex.codeFor("IDNT")) {  //must be an ASSIGNMENT
            recur = handleAssignment();
        } else {
            if (token.code == lex.codeFor("IF__")) {  //must be an IF
            	recur = handleIf();
            } else if (token.code == lex.codeFor("FOR_")) {
            	// must be a FOR
            	recur = handleFor();
            } else if (token.code == lex.codeFor("WHIL")) {
            	// must be a while
            	recur = handleWhile();
            } else if (token.code == lex.codeFor("REPT")) {
            	// must be a repeat
            	recur = handleRepeat();
            } else if (token.code == lex.codeFor("GOTO")) {
            	// must be a goto
            	recur = handleGoto();
            } else if (token.code == lex.codeFor("RDLN")) {
            	// must be a readln
            	recur = handleReadln();
            } else if (token.code == lex.codeFor("WRLN")) {
            	// must be a writeln
            	recur = handleWriteln();
            } else if (token.code == lex.codeFor("BGIN")) {
            	// must be a block body
            	recur = Block();
            } else {
                error("Statement start", token.lexeme);
            }
        }

        trace("Statement", false);
        return recur;
    } // Statement

    //Non-terminal VARIABLE just looks for an IDENTIFIER.  Later, a
    //  type-check can verify compatible math ops, or if casting is required.    
    private int Variable() {
        int recur = 0;
        if (anyErrors) {
            return -1;
        }

        trace("Variable", true);
        if (token.code == lex.codeFor("IDNT")) {
        	
        	// get symbol index and store its address for later return
        	int symbolIndex = symbolList.LookupSymbol(token.lexeme);
        	recur = symbolList.GetAddress(symbolIndex);
        	
        	// if identifier is being declared beyond variable declaration section and it has an undeclared type, then it is an undeclared identifier
        	if(symbolList.GetFinishDeclarations() && symbolList.isUndeclared(symbolIndex)) {
        		// print out an error, but continue execution
        		System.out.println("ERROR: Undeclared Identifier " + token.lexeme + ". Continuing...");
        		
        		// We want the build to file but the rest of compilation may be error free, so mark an error but don't set the function escape error flag
        		errorsFound = true;
        		
        		// set the identifier's type and purpose and move on. prevents this error from printing multiple times. We have not yet consumed the type, so it will default to label
        		symbolList.SetType(symbolIndex, DataType.LABELTYPE);
        		symbolList.SetPurpose(symbolIndex, null);
        	} else if (symbolList.GetPurpose(symbolIndex) == SymbolPurpose.LABEL) {
        		// Identifier w/ purpose of LABEL is being used as a variable. Continue parsing but set error flag
        		System.out.println("ERROR: " + token.lexeme + " is being used as a VARIABLE, but is a LABEL. Continuing...");
        		errorsFound = true;
        	} else if (symbolList.GetPurpose(symbolIndex) == SymbolPurpose.PROGRAM) {
        		// Identifier /w purpose of PROGRAM is being used as a variable. Continue parsing but set error flag
        		System.out.println("ERROR: " + token.lexeme + " is being used as a VARIABLE, but is a PROGRAM. Continuing...");
        		errorsFound = true;
        	}
        	
            // bookkeeping and move on
            token = lex.GetNextToken();
        } else {
            error("Variable", token.lexeme);
        }

        trace("Variable", false);
        return recur;

    } // Variable
    
    // terminal UnsignedConstant which checks for valid literals
    private int UnsignedConstant() {
    	int recur = 0;
    	if(anyErrors) {
    		return -1;
    	}
    	
    	trace("UnsignedConstant", true);
    	
    	if(token.code == lex.codeFor("INTL") || token.code == lex.codeFor("FLTL")) {
    		recur = UnsignedNumber();
    	} else if (token.code == lex.codeFor("STRL")) {
    		recur = UnsignedString();
    	} else {
    		error(lex.reserveFor("INTL") + " / " + lex.reserveFor("FLTL") + " / " + lex.reserveFor("STRL"), token.lexeme);
    	}
    	
    	trace("UnsignedConstant", false);
    	return recur;
    } // UnsignedConstant
    
    // checks for valid number literals
    private int UnsignedNumber() {
    	int recur = 0;
    	if(anyErrors) {
    		return -1;
    	}
    	
    	trace("UnsignedNumber", true);
    	
    	if(token.code == lex.codeFor("INTL")) {
    		// check if literal already has memory allocated
    		int index = symbolList.LookupSymbol(token.lexeme);
    		int address = symbolList.GetAddress(index);
    		if(address == 0) {
    			// if not, allocate memory for the literal and store its address
    			address = AllocateMem(DataType.INTEGERTYPE);
    			symbolList.PutAddress(index,  address);
    			mem.put(Integer.parseInt(token.lexeme), address);
    		}
    		   		
    		token = lex.GetNextToken();
    		recur = address;
    	} else if(token.code == lex.codeFor("FLTL")) {
    		// check if literal already has memory allocated
    		int index = symbolList.LookupSymbol(token.lexeme);
    		int address = symbolList.GetAddress(index);
    		if(address == 0) {
    			// if not, allocate memory for the literal and store its address
    			address = AllocateMem(DataType.FLOATTYPE);
    			symbolList.PutAddress(index,  address);
    			mem.put(Double.parseDouble(token.lexeme), address);
    		}
    		   		
    		token = lex.GetNextToken();
    		recur = address;
    	} else {
    		// expected an integer or float literal but received something else
    		error(lex.reserveFor("INTL") + " / " + lex.reserveFor("FLTL"), token.lexeme);
    	}
    	
    	trace("UnsignedNumber", false);
    	return recur;
    } // UnsignedNumber
    
    // checks for valid string literals
    private int UnsignedString() {
    	int recur = 0;
    	if(anyErrors) {
    		return -1;
    	}
    	
    	trace("UnsignedString", true);
    	
    	if(token.code == lex.codeFor("STRL")) {
    		// check if literal already has memory allocated
    		int index = symbolList.LookupSymbol(token.lexeme);
    		int address = symbolList.GetAddress(index);
    		if(address == 0) {
    			// if not, allocate memory for the literal and store its address
    			address = AllocateMem(DataType.STRINGTYPE);
    			symbolList.PutAddress(index, address);
    			
    			// stripping out the quotes from the string so they are not present in prints
    	    	if(token.lexeme.length() > 1 && token.lexeme.startsWith("\"") && token.lexeme.endsWith("\"")) {
    	    		// only strip if string has more than quotes in it. Uses regex to replace quotes w/ null
    	    		mem.put(token.lexeme.replaceAll("\"", ""), address);
    	    	} else {
    	    		// empty string, leave quotes
    	    		mem.put(token.lexeme, address);
    	    	}
    	    	
    		}
    		
    		token = lex.GetNextToken();
    		recur = address;
    	} else {
    		// expected an integer or float literal but received something else
    		error(lex.reserveFor("STRL"), token.lexeme);
    	}
    	
    	trace("UnsignedString", false);
    	return recur;
    } // UnsignedString
    
    // allocates memory in the datamem table based on the passed type, and inits to 0/empty. Returns address of the newly allocated memory
    private int AllocateMem(DataType type) {
    	int address;
    	
    	// call the correct method overload based on input data type
    	switch(type) {
    		case INTEGERTYPE:
    			address = mem.Malloc(0);
    			break;
    		case STRINGTYPE:
    			address = mem.Malloc("");
    			break;
    		case FLOATTYPE:
    			address = mem.Malloc(0.0);
    			break;
    		default:
    			address = -1;
    	}
    	
    	return address;
    } // AllocateMem
    
    /**
     * *************************************************
     */
    /*     UTILITY FUNCTIONS USED THROUGHOUT THIS CLASS */
	// error provides a simple way to print an error statement to standard output
	//     and avoid reduncancy
    private void error(String wanted, String got) {
        anyErrors = true;
        errorsFound = true;
        System.out.println("ERROR: Expected " + wanted + " but found " + got);
    } // error



    // trace simply RETURNs if traceon is false; otherwise, it prints an
    // ENTERING or EXITING message using the proc string
    private void trace(String proc, boolean enter) {
        String tabs = "";

        if (!traceon) {
            return;
        }

        if (enter) {
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("--> Entering " + proc);
            level++;
        } else {
            if (level > 0) {
                level--;
            }
            tabs = repeatChar(" ", level);
            System.out.print(tabs);
            System.out.println("<-- Exiting " + proc);
        }
    } // trace

    
    
    // repeatChar returns a string containing x repetitions of string s; 
    //    nice for making a varying indent format
    private String repeatChar(String s, int x) {
        int i;
        String result = "";
        for (i = 1; i <= x; i++) {
            result = result + s;
        }
        return result;
    } // repeatChar
    
} // Syntactic
