// Logan Morsman - SP26 CS4100-001
package ADT;

import java.util.Scanner;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;

public class Interpreter {
	
	private final int MAXQUAD = 1000; // maximum quad capacity
	private final int MAXOPS = 16; // number of known operands
	private ReserveTable optable; // table to store operands
	
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
	
	// quad interpretations
	private final int OPCODE = 0;
	private final int OP1 = 1;
	private final int OP2 = 2;
	private final int OP3 = 3;
	
	// constructor
	public Interpreter () {
		optable = new ReserveTable(MAXOPS); // initialize optable 
		initReserve(optable); // add known operands to optable
	} // constructor
	
	public void InterpretQuads(QuadTable Quads, DataMem Data, boolean TraceOn, String filename) {
		
		// initialization phase. prepare to read and store quads
		int programCounter = 0;
		int quad[] = new int[4];
		
		// while there are still quads to read
		while(programCounter < MAXQUAD) {
			
			quad = Quads.GetQuad(programCounter); // get next quad
			
			// if tracing is enabled
			if(TraceOn) {
				
				String traceOutput = makeTraceString(programCounter, quad[OPCODE], quad[OP1], quad[OP2], quad[OP3]); // generate trace string
				System.out.println(traceOutput); // print trace string to console
				
				// file print handling
				try {
					// open file writers
					FileWriter fw = new FileWriter(filename, true); // append to file
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);
					
					pw.println(traceOutput); // print trace string to file
					
					// close file writers
					pw.close();
					bw.close();
					fw.close();
				} catch (IOException e) {
					System.err.println("Unable to print trace to file with error: " + e.getMessage());
					e.printStackTrace();
				}
				
			}
			
			// verify returned opcode is a known operation
			if(quad[OPCODE] >= STOP_OPCODE && quad[OPCODE] <= JINDR_OPCODE) {
				
				// operation processing
				switch (quad[OPCODE]) {
				
					// kill loop, stop reading quads
					case STOP_OPCODE: 
						programCounter = MAXQUAD; // loop termination
						System.out.println("Execution terminated by program STOP.");
						break;
						
					// store op1 / op2 in op3
					case DIV_OPCODE: 
						Data.put(Data.getInt(quad[OP1]) / Data.getInt(quad[OP2]), quad[OP3]); // take floor of div op for float -> int type casting
						programCounter++; // increment to next quad
						break;
						
					// store op1 * op2 in op3
					case MUL_OPCODE: 
						Data.put(Data.getInt(quad[OP1]) * Data.getInt(quad[OP2]), quad[OP3]);
						programCounter++; // increment to next quad
						break;
						
					// store op1 - op2 in op3
					case SUB_OPCODE: 
						Data.put(Data.getInt(quad[OP1]) - Data.getInt(quad[OP2]), quad[OP3]);
						programCounter++; // increment to next quad
						break;
						
					// store op1 + op2 in op3
					case ADD_OPCODE:  
						Data.put(Data.getInt(quad[OP1]) + Data.getInt(quad[OP2]), quad[OP3]);
						programCounter++; // increment to next quad
						break;
						
					// store op1 in op3
					case MOV_OPCODE: 
						Data.put(Data.getInt(quad[OP1]), quad[OP3]);
						programCounter++; // increment to next quad
						break;
						
					// print value at op3
					case PRINT_OPCODE: 
						if(Data.isInteger(quad[OP3])) {
							System.out.println(Data.getInt(quad[OP3]));
						} else if(Data.isString(quad[OP3])) {
							System.out.println(Data.getString(quad[OP3]));
						} else if(Data.isFloat(quad[OP3])) {
							System.out.println(Data.getFloat(quad[OP3]));
						} else {
							System.out.println("Value at Memory Slot " + Integer.toString(quad[OP3]) + " = " + Integer.toString(Data.getInt(quad[OP3]))); // This is printing memory addresses
						}
						programCounter++; // increment to next quad
						break;
					
					// read stdin and store to op1
					case READ_OPCODE: 
						Scanner scanner = new Scanner(System.in); // open scanner
						System.out.print("> ");
						Data.put(scanner.nextInt(), quad[OP3]); // read next int from console and store to op1
						scanner.close(); // close scanner
						programCounter++; // increment to next quad
						break;
						
					// jump
					case JMP_OPCODE: 
						programCounter = quad[OP3]; // jump programCounter
						break;
						
					// jump if zero
					case JZ_OPCODE: 
						// check if data equals 0
						if(Data.getInt(quad[OP1]) == 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump if positive
					case JP_OPCODE: 
						// check if data is positive
						if(Data.getInt(quad[OP1]) > 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump if negative
					case JN_OPCODE: 
						// check if data is negative
						if(Data.getInt(quad[OP1]) < 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump not zero
					case JNZ_OPCODE: 
						// check if data is not 0
						if(Data.getInt(quad[OP1]) != 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump if not positive
					case JNP_OPCODE: 
						// check if data is not positive
						if(Data.getInt(quad[OP1]) <= 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump if not negative
					case JNN_OPCODE:
						// check if data is not negative
						if(Data.getInt(quad[OP1]) >= 0) {
							programCounter = quad[OP3]; // jump programCounter
						} else {
							programCounter++; // increment to next quad
						}
						
						break;
						
					// jump indirect. set programCounter to symbol value found at OP3 index
					case JINDR_OPCODE: 
						programCounter = quad[OP3]; // jump programCounter
						break;
						
					// error handling. This should never be reached
					default:
						System.out.println("Invalid opcode " + Integer.toString(quad[OPCODE]) + " was read from quad.");
						programCounter = MAXQUAD;
				}
					
			}
			
		}
		
	} // InterpretQuads
	
	// creates a trace of the operation currently being interpreted
	private String makeTraceString(int pc, int opcode, int op1, int op2, int op3) {
        String result = "";
        result = "PC = " + String.format("%04d", pc) + ": " + (optable.LookupCode(opcode) + "     ").substring(0, 6) + String.format("%02d", op1)
                + ", " + String.format("%02d", op2) + ", " + String.format("%02d", op3);
        return result;
    } // makeTraceString
 
	// fill optable with known operands
    private void initReserve(ReserveTable optable) {
        optable.Add("STOP", STOP_OPCODE);
        optable.Add("DIV", DIV_OPCODE);
        optable.Add("MUL", MUL_OPCODE);
        optable.Add("SUB", SUB_OPCODE);
        optable.Add("ADD", ADD_OPCODE);
        optable.Add("MOV", MOV_OPCODE);
        optable.Add("PRINT", PRINT_OPCODE);
        optable.Add("READ", READ_OPCODE);
        optable.Add("JMP", JMP_OPCODE);
        optable.Add("JZ", JZ_OPCODE);
        optable.Add("JP", JP_OPCODE);
        optable.Add("JN", JN_OPCODE);
        optable.Add("JNZ", JNZ_OPCODE);
        optable.Add("JNP", JNP_OPCODE);
        optable.Add("JNN", JNN_OPCODE);
        optable.Add("JINDR", JINDR_OPCODE);
    } // initReserve
    
} // Interpreter
