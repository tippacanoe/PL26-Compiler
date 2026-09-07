package ADT;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class QuadTable {

	private int nextAvailable;
	private int quads[][];
	private int maxSize;
	
	// constructor
	public QuadTable(int maxSize) {
		this.maxSize = maxSize;
		quads = new int[this.maxSize][4]; // maxSize x 4, each row is a different operation. 4 cols for the operands and opcode
		nextAvailable = 0;
	} // constructor
	
	// return nextAvailable row in the QuadTable
	public int NextQuad() {
		return nextAvailable;
	} // NextQuad
	
	// adds a new quad to the table and increments the nextAvailable counter
	public void AddQuad(int opcode, int op1, int op2, int op3) {
		quads[nextAvailable][0] = opcode;
		quads[nextAvailable][1] = op1;
		quads[nextAvailable][2] = op2;
		quads[nextAvailable][3] = op3;
		nextAvailable++;
	} // AddQuad
	
	// return one quad in the table
	public int[] GetQuad(int index) {
		return quads[index];
	} // GetQuad
	
	// update the jump operand for a row to a new value
	public void UpdateJump(int index, int op3) {
		quads[index][3] = op3;
	} // UpdateJump
	
	// prints formatted QuadTable to the specified file
	public void PrintQuadTable(String filename) {
		
		// initialization phase
		int indexLength = 7;
		int longestOpcodeLength = 7;
		int longestOp1Length = 7;
		int longestOp2Length = 7;
		//int longestOp3Length = 7;
		
		// check for table entries with more characters than allotted for printing
		for(int i = 0; i < nextAvailable; i++) {
			
			// calculate the number of base10 digits required to represent each int in QuadTable 
			int operationCodeLength = ((int) Math.floor(Math.log(quads[i][0])) + 3); 
			int op1Length = ((int) Math.floor(Math.log(quads[i][1])) + 3); 
			int op2Length = ((int) Math.floor(Math.log(quads[i][2])) + 3); 
			//int op3Length = ((int) Math.floor(Math.log(quads[i][3])) + 3); 
			
			// if the number of digits required to store part of quad is longer than known longest, update col spacing
			if(operationCodeLength > longestOpcodeLength) { longestOpcodeLength = operationCodeLength; }
			if(op1Length > longestOp1Length) { longestOp1Length = op1Length; }
			if(op2Length > longestOp2Length) { longestOp2Length = op2Length; }
			//if(op3Length > indexLength) { longestOp3Length = op3Length; }
			
		}
		
		// if there are more than 10^7 entries in table, expand the col size of index
		if(Math.log(nextAvailable) > indexLength) {
			indexLength = ((int) Math.floor(Math.log(nextAvailable))) + 3;
		}
		
		// file writing error protection
		try {
			
			// open file and prepare output stream
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter); // included for printf support
			
			// table headers
			printWriter.printf("%-" + indexLength + "s%-" + longestOpcodeLength + "s%-" + longestOp1Length + "s%-" + longestOp2Length + "s%s%n", "Index", "Opcode", "op1", "op2", "op3");
			
			// print all entries in quad table
			for(int i = 0; i < nextAvailable; i++) {
				printWriter.printf("%-" + indexLength + "d%-" + longestOpcodeLength + "d%-" + longestOp1Length + "d%-" + longestOp2Length + "d%d%n", i, quads[i][0], quads[i][1], quads[i][2], quads[i][3]);
			}
			
			// close output streams and file
			printWriter.close();
			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
	} // PrintQuadTable
	
} // QuadTable
