package ADT;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ReserveTable {

	private int maxSize; // maximum entries in table
	private String[] names; // names of entries in table
	private int[] codes; // opcodes of entries in table
	private int usedRows; // number of used entries in table
	
	// constructor
	ReserveTable(int maxSize) {
		this.maxSize = maxSize;
		names = new String[this.maxSize];
		codes = new int[this.maxSize];
		usedRows = 0;
	} // ReserveTable
	
	// add a new row to table
	int Add(String name, int code) {
		names[usedRows] = name;
		codes[usedRows] = code;
		usedRows++;
		return usedRows - 1;
	} // Add
	
	// search for code associated with name
	int LookupName(String name) {
		
		// initialization phase
		int returnCode = -1; // -1 indicates value was not found
		int counter = 0;

		// loop through all saved names in table, and return corresponding code if found
		while(counter < usedRows) {
			if(names[counter].compareToIgnoreCase(name) == 0) {
				returnCode = codes[counter];
				counter = usedRows;
			}
			counter++;
		}
		
		return returnCode;
		
	} // LookupName
	
	// search for name associated with code
	String LookupCode(int code) {
		
		// initialization phase
		String returnName = ""; // blank name indicates value was not found
		int counter = 0;
		
		// loop through all saved codes in table, and return corresponding name if found
		while(counter < usedRows) {
			if(codes[counter] == code) {
				returnName = names[counter];
				counter = usedRows;
			}
			counter++;
		}
		
		return returnName;
		
	} // LookupCode
	
	// print full table to console
	void PrintReserveTable(String filename) {
		
		// initialization phase
		int indexLength = 7; // minimum col spacing
		int longestNameLength = names[0].length() + indexLength;
		
		// if there are more than 10^7 entries in table, expand the col size of index
		if(Math.log(usedRows) > indexLength) {
			indexLength = ((int) Math.floor(Math.log(usedRows))) + 3;
		}
	
		// find the longest name in the reserve table and store its length for print formatting purposes
		if(usedRows > 1) {
			for(int i = 1; i < usedRows; i++) {
				if(names[i].length() > names[i - 1].length()) {
					longestNameLength = names[i].length();
				}
			}
		}
		
		// add additional col spacing
		longestNameLength += 3;
		
		// file writing error protection
		try {
			
			// open file and prepare output stream
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter); // included for printf support
			
			// table headers
			printWriter.printf("%-" + indexLength + "s%-" + longestNameLength + "s%s%n", "Index", "Name", "Code");
		
			// print all entries in reserve table
			for(int i = 0; i < usedRows; i++) {
				printWriter.printf("%-" + indexLength + "d%-" + longestNameLength + "s%d%n", i, names[i], codes[i]);
			}
			
			// close output streams and file
			printWriter.close();
			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
	} // PrintReserveTable
	
} // ReserveTable
