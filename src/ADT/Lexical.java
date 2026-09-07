package ADT;

/*
 The following code is provided by the instructor for the completion of PHASE 2 
of the compiler project for CS4100/5100.

SPRING 2026 version

STUDENTS ARE TO PROVIDE THE FOLLOWING CODE FOR THE COMPLETION OF THE ASSIGNMENT:

1) USE THE LEXICAL CONSTRUCTOR GIVEN BELOW!
Initialize the 2 reserve tables, which are fields in the Lexical class,
    named reserveWords and mnemonics; example code appears below
    These calls are in the lexical constructor:
        initReserveWords(reserveWords);
        initMnemonics(mnemonics);

PROVIDED UTILITY FUNCTIONS

// ********************** UTILITY FUNCTIONS
consoleShowError(String message), easily output an error message

isLetter(char ch), isDigit(char ch), isStringStart(char ch) 

doubleOK(String stin) returns true if stin is a valid Java double
integerOK(String stin) returns true if stin is a valid Java int

The following are place holders which the student must implement
getIdent(), getNumber(), getString(), getOneTwoChar()

 dummyGet() gets one character, makes it a token, just to verify
      file access is working

public token GetNextToken() - the framework is shown
 
 */
/**
 * @author lmorsman
 */

public class Lexical {

	//global char access throughout
    char currCh;

    //  sent as parameter to Lexical constructor
    private SymbolTable saveSymbols;          //SymbolTable used in Lexical

    private LexScanner scan;                 //file handling
    
    private boolean printToken;               //true to print found tokens here, used later

    //Tables to hold the reserve words and the mnemonics for token codes
    private final int sizeReserveTable = 50;
    private ReserveTable reserveWords = new ReserveTable(sizeReserveTable); //a few more than # reserves
    private ReserveTable mnemonics = new ReserveTable(sizeReserveTable); //a few more than # reserves

    
    //constructor
    public Lexical(String filename, SymbolTable symbols, boolean echoOn) {
        printToken = false;     //default OFF, do not print tokesn here
        //  within GetNextToken; call setPrintToken to
        //  change it publicly.

        saveSymbols = symbols;  //map the initialized parameter to a local ST name

        //Create and initialize a scanner for file access
        scan = new LexScanner(filename, echoOn);

        currCh = scan.GetNextChar();  //get first character from file

        //call initializations of  reserve tables
        initReserveWords(reserveWords);
        initMnemonics(mnemonics);
    } // constructor

    // inner class "token" is declared here, no accessors needed
    public class token {

        public String lexeme;
        public int code;
        public String mnemonic;

        token() {
            lexeme = "";
            code = 0;
            mnemonic = "";
        }
    } // token

    // Public access to the current End Of File status from LexScanner
    public boolean EOF() {
        return scan.EOF();
    } // EOF

    //Token codes for the     
    private final int IDENT_ID = 50;
    private final int INTEGER_ID = 51;
    private final int FLOAT_ID = 52;
    private final int STRING_ID = 53;
    private final int NDEF_ID = 99;
    private final int INVALID_ID = -1;
    
    // token length limits
    private final int IDENT_LENGTH = 20;
    private final int INTEGER_LENGTH = 6;
    private final int FLOAT_LENGTH = 12;

    // ******************* PUBLIC USEFUL METHODS
    // These are nice for syntax to call later 
    // given a mnemonic, find its token code value
    public int codeFor(String mnemonic) {
        return mnemonics.LookupName(mnemonic);
    } // codeFor
    // given a mnemonic, return its reserve word

    public String reserveFor(String mnemonic) {
        return reserveWords.LookupCode(mnemonics.LookupName(mnemonic));
    } // reserveFor

    // DEBUG enabler, turns on/OFF token printing inside of GetNextToken
    public void setPrintToken(boolean on) {
        printToken = on;
    } // setPrintToken

    private void initReserveWords(ReserveTable reserveWords) {
        reserveWords.Add("GOTO", 0);
        reserveWords.Add("INTEGER", 1);
        reserveWords.Add("TO", 2);
        reserveWords.Add("DO", 3);
        reserveWords.Add("IF", 4);
        reserveWords.Add("THEN", 5);
        reserveWords.Add("ELSE", 6);
        reserveWords.Add("FOR", 7);
        reserveWords.Add("OF", 8);
        reserveWords.Add("WRITELN", 9);
        reserveWords.Add("READLN", 10);
        reserveWords.Add("BEGIN", 11);
        reserveWords.Add("END", 12);
        reserveWords.Add("VAR", 13);
        reserveWords.Add("WHILE", 14);
        reserveWords.Add("PROGRAM", 15);
        reserveWords.Add("LABEL", 16);
        reserveWords.Add("REPEAT", 17);
        reserveWords.Add("UNTIL", 18);
        reserveWords.Add("FLOAT", 19);
        reserveWords.Add("REAL", 20);
        reserveWords.Add("STRING", 21);
        
        //1 and 2-char
        reserveWords.Add("/", 30);
        reserveWords.Add("*", 31);
        reserveWords.Add("+", 32);
        reserveWords.Add("-", 33);
        reserveWords.Add("(", 34);
        reserveWords.Add(")", 35);
        reserveWords.Add(";", 36);
        reserveWords.Add(":=", 37);
        reserveWords.Add(">", 38);
        reserveWords.Add("<", 39);
        reserveWords.Add(">=", 40);
        reserveWords.Add("<=", 41);
        reserveWords.Add("=", 42);
        reserveWords.Add("<>", 43);
        reserveWords.Add(",", 44);
        reserveWords.Add("[", 45);
        reserveWords.Add("]", 46);
        reserveWords.Add(":", 47);
        reserveWords.Add(".", 48);
        reserveWords.Add("NDEF", 99);

    } // initReserveWords

    private void initMnemonics(ReserveTable mnemonics) {
    	// reserved mnemonics
        mnemonics.Add("GOTO", 0);
        mnemonics.Add("INTG", 1);
        mnemonics.Add("TO__", 2);
        mnemonics.Add("DO__", 3);
        mnemonics.Add("IF__", 4);
        mnemonics.Add("THEN", 5);
        mnemonics.Add("ELSE", 6);
        mnemonics.Add("FOR_", 7);
        mnemonics.Add("OF__", 8);
        mnemonics.Add("WRLN", 9);
        mnemonics.Add("RDLN", 10);
        mnemonics.Add("BGIN", 11);
        mnemonics.Add("END_", 12);
        mnemonics.Add("VAR_", 13);
        mnemonics.Add("WHIL", 14);
        mnemonics.Add("PROG", 15);
        mnemonics.Add("LABL", 16);
        mnemonics.Add("REPT", 17);
        mnemonics.Add("UNTL", 18);
        mnemonics.Add("FLOT", 19);
        mnemonics.Add("REAL", 20);
        mnemonics.Add("STRN", 21);
        
        //1 and 2-char
        mnemonics.Add("DIVD", 30);
        mnemonics.Add("MULT", 31);
        mnemonics.Add("PLUS", 32);
        mnemonics.Add("MNUS", 33);
        mnemonics.Add("LPTS", 34);
        mnemonics.Add("RPTS", 35);
        mnemonics.Add("SMCL", 36);
        mnemonics.Add("CLEQ", 37);
        mnemonics.Add("GT__", 38);
        mnemonics.Add("LT__", 39);
        mnemonics.Add("GTEQ", 40);
        mnemonics.Add("LTEQ", 41);
        mnemonics.Add("EQ__", 42);
        mnemonics.Add("NEQ_", 43);
        mnemonics.Add("COMA", 44);
        mnemonics.Add("LBRC", 45);
        mnemonics.Add("RBRC", 46);
        mnemonics.Add("COLN", 47);
        mnemonics.Add("DOT_", 48);
        mnemonics.Add("NDEF", 99);
        
        // literals
        mnemonics.Add("IDNT", IDENT_ID);
        mnemonics.Add("INTL", INTEGER_ID);
        mnemonics.Add("FLTL", FLOAT_ID);
        mnemonics.Add("STRL", STRING_ID);
    } // initMnemonics

    // ********************** UTILITY FUNCTIONS
    private void consoleShowError(String message) {
        System.out.println("**** ERROR FOUND: " + message);
    } // consoleShowError

    // Character category for alphabetic chars
    private boolean isLetter(char ch) {
        return (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')));
    } // isLetter

    // Character category for 0..9 
    private boolean isDigit(char ch) {
        return ((ch >= '0') && (ch <= '9'));
    } // isDigit

    private boolean isPrefix(char ch) {
        return ((ch == ':') || (ch == '<') || (ch == '>'));
    } // isPrefix
    
    private boolean isStringStart(char ch) {
        return ch == '"';
    } // isStringStart

    public boolean doubleOK(String stin) {
        boolean result;
        Double x;
        try {
            x = Double.parseDouble(stin);
            result = true;
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    } // doubleOK

    public boolean integerOK(String stin) {
        boolean result;
        int x;
        try {
            x = Integer.parseInt(stin);
            result = true;
        } catch (NumberFormatException ex) {
            result = false;
        }
        return result;
    } // integerOK

    // returns an identifier token and saves to symbol table if not stored already
    private token getIdent() {
    	token result = new token();
    	result.lexeme += currCh;
    	currCh = scan.GetNextChar();
    	
    	while(isDigit(currCh) || isLetter(currCh) || currCh == '_') {
    		result.lexeme += currCh;
    		currCh = scan.GetNextChar();
    	}
    	
    	// check if identifier is longer than maximum length 20
    	if(result.lexeme.length() > IDENT_LENGTH) {
    		consoleShowError("IDENTIFIER LENGTH EXCEEDS LIMIT. TRUNCATING " + result.lexeme + " TO " + result.lexeme.substring(0, IDENT_LENGTH));
    		result.lexeme = result.lexeme.substring(0, IDENT_LENGTH); // truncates lexeme
    	}
    	
    	// check if identifier is a known reserve word
    	if(reserveWords.LookupName(result.lexeme) != INVALID_ID) {
    		result.code = reserveWords.LookupName(result.lexeme); // assign reserve word code to token
    	} else {
    		result.code = IDENT_ID; // assign the identifier ID
    		// this is a new identifier. Save to symbol table
    		saveSymbols.AddSymbol(result.lexeme, SymbolPurpose.VARIABLE, DataType.UNDECTYPE); // all new identifiers enter table undefined. syntax analyzer will fix this
    	}
    	
    	result.mnemonic = mnemonics.LookupCode(result.code);
        return result;
    } // getIdent

    // returns token of number
    private token getNumber() {
        //      /* a number is:   <digit>+[.<digit>*[E[+|-]<digit>+]]
    	token result = new token();
    	result.lexeme += currCh;
    	
    	// loop until something other than a digit is found
    	while(isDigit(scan.PeekNextChar())) {
    		currCh = scan.GetNextChar();
    		result.lexeme += currCh;
    	}
    	
    	// if number is a float
    	if(scan.PeekNextChar() == '.') {
    		currCh = scan.GetNextChar();
    		result.lexeme += currCh;
    		
    		// check for digits after float
        	while(isDigit(scan.PeekNextChar())) {
        		currCh = scan.GetNextChar();
        		result.lexeme += currCh;
        	}
        	
        	// if E float detected
        	if(scan.PeekNextChar() == 'E' || scan.PeekNextChar() == 'e') {
        		currCh = scan.GetNextChar();
        		result.lexeme += currCh;
        		
        		// if valid sign identified found
        		if(scan.PeekNextChar() == '+' || scan.PeekNextChar() == '-') {
        			currCh = scan.GetNextChar();
        			result.lexeme += currCh;
        		}
        			
    			// confirm at least one digit found
    			if(isDigit(scan.PeekNextChar())) {
    				currCh = scan.GetNextChar();
        			result.lexeme += currCh;
        			
        			// loop until something other than a digit is found
        	    	while(isDigit(scan.PeekNextChar())) {
        	    		currCh = scan.GetNextChar();
        	    		result.lexeme += currCh;
        	    	}
    			}
        		
        	}
    	}
    	
    	currCh = scan.GetNextChar(); // increment for next token
    	
    	// check that number is a valid integer or float
    	if(integerOK(result.lexeme)) {
    		// assign integer code to token
    		result.code = INTEGER_ID;
    		
    		// check if integer is longer than maximum length 6
        	if(result.lexeme.length() > INTEGER_LENGTH) {
        		consoleShowError("INTEGER LENGTH EXCEEDS LIMIT. TRUNCATING " + result.lexeme + " TO " + result.lexeme.substring(0, INTEGER_LENGTH));
        		result.lexeme = result.lexeme.substring(0, INTEGER_LENGTH); // truncates lexeme
        	}
        	
        	// add literal to symbol table
        	saveSymbols.AddSymbol(result.lexeme, SymbolPurpose.CONSTANT, DataType.INTEGERTYPE);
        	
    	} else if (doubleOK(result.lexeme)) {
    		// assign float code to token
    		result.code = FLOAT_ID;
    		
    		// check if float is longer than maximum length 12
        	if(result.lexeme.length() > FLOAT_LENGTH) {
        		consoleShowError("FLOAT LENGTH EXCEEDS LIMIT. TRUNCATING " + result.lexeme + " TO " + result.lexeme.substring(0, FLOAT_LENGTH));
        		result.lexeme = result.lexeme.substring(0, FLOAT_LENGTH); // truncates lexeme
        	}
        	
        	// add literal to symbol table
        	saveSymbols.AddSymbol(result.lexeme, SymbolPurpose.CONSTANT, DataType.FLOATTYPE);
    	} else {
    		// token is NOT a number. return not defined token code
    		result.code = 99; // assign token code
    	}
    	
    	result.mnemonic = mnemonics.LookupCode(result.code); // assign token mnemonic
        return result;
    } // getNumber

    // returns string token. Excludes line terminators
    private token getString() {
    	token result = new token();
    	result.lexeme += currCh;
    	currCh = scan.GetNextChar();
    	
    	// loop until closing quotes are found or file ends or line terminating character is seen
    	while(currCh != '"' && !scan.EOF() && currCh != '\n' && currCh != '\r') {
    		result.lexeme += currCh;
    		currCh = scan.GetNextChar();
    	}
    	
    	// unterminated string
    	if(scan.EOF() || currCh == '\n' || currCh == '\r') {
    		consoleShowError("UNTERMINATED STRING FOUND.");
    		result.code = NDEF_ID; // assign not defined ID
    	} else {
    		// add closing quote to string
        	result.lexeme += currCh;
        	result.code = STRING_ID; // set string code
        	saveSymbols.AddSymbol(result.lexeme, SymbolPurpose.CONSTANT, DataType.STRINGTYPE);
    	}
    	
    	currCh = scan.GetNextChar(); // prep next token
    	result.mnemonic = mnemonics.LookupCode(result.code); // set string mnemonic
        return result;
    } // getString

    // returns valid token if it is a OneTwoChar
    private token getOneTwoChar() {
    	token result = new token();
    	result.lexeme += currCh;
    	
    	// if two char
    	if(isPrefix(currCh)) {
    		// if valid two char
    		if(currCh == '<' && scan.PeekNextChar() == '>') {
    			currCh = scan.GetNextChar();
    			result.lexeme += currCh;
    		} else if(scan.PeekNextChar() == '=') {
    			currCh = scan.GetNextChar();
    			result.lexeme += currCh;
    		}
    	}
    	
    	currCh = scan.GetNextChar(); // increment for next token
    	result.code = reserveWords.LookupName(result.lexeme); // assign token code
    	// check if this one-char is not defined in reserve table, and assign not defined code
    	if(result.code == INVALID_ID) {
    		result.code = NDEF_ID;
    	}
    	result.mnemonic = mnemonics.LookupCode(result.code); // assign token mnemonic
        return result;
    } // getOneTwoChar

    public token GetNextToken() {
        token result = new token();

        currCh = scan.skipWhiteSpace(currCh);
        if (isLetter(currCh) || currCh == '_') { //is ident
            result = getIdent();
        } else if (isDigit(currCh)) { //is numeric
            result = getNumber();
        } else if (isStringStart(currCh)) { //string literal
            result = getString();
        } else //default char checks
        {
            result = getOneTwoChar();
        }

        if ((result.lexeme.equals("")) || (scan.EOF())) {
            result = null;
        }
        
        //set the mnemonic
        if (result != null) {

            result.mnemonic = mnemonics.LookupCode(result.code);
            // do truncation step HERE, modify result;
            if (printToken) {
                System.out.println("\t" + result.mnemonic + " | \t" + String.format("%04d", result.code) + " | \t" + result.lexeme);
            }
        }
        return result;

    } // GetNextToken

} // Lexical
