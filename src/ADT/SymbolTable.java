package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;


public class SymbolTable {
	
	private String tokens[]; // lexeme of token
	private SymbolPurpose purposes[]; // purpose of token
	private DataType types[]; // type of token
	private int addresses[]; // address of token
	private int nextAvailable; // next open row in table
	private int maxSize; // maximum rows in table
	private boolean finishDeclarations;
	
	// constructor
	public SymbolTable(int maxSize) {
		this.maxSize= maxSize;
		nextAvailable = 0;
		tokens = new String[this.maxSize];
		purposes = new SymbolPurpose[this.maxSize];
		types = new DataType[this.maxSize];
		addresses = new int[this.maxSize];
		finishDeclarations = false;
		
		// add constants that are always present
		AddSymbol("0", SymbolPurpose.CONSTANT, DataType.INTEGERTYPE);
		AddSymbol("1", SymbolPurpose.CONSTANT, DataType.INTEGERTYPE);
		AddSymbol("-1", SymbolPurpose.CONSTANT, DataType.INTEGERTYPE);
		
	} // constructor
	
	/* Adds a symbol to the table. 
	 * First checks for existing symbols to prevent conflicts. 
	 * Returns index of newly created (or pre-existing) symbol. 
	 * Returns -1 if no space is available in the table.
	 */
	public int AddSymbol(String symbol, SymbolPurpose purpose, DataType datatype) {
		
		int index = -1; // tracks if symbol already exists in table, and is used to return existing or newly created symbol location
		
		// check if there is an open row in the table before attempting to create any symbols
		if(nextAvailable < maxSize) {
			
			// check if symbol already exists in table
			index = LookupSymbol(symbol);
			
			// create symbol if symbol does not exist in table
			if(index == -1) {
				tokens[nextAvailable] = symbol;
				purposes[nextAvailable] = purpose;
				if(!finishDeclarations) {
					types[nextAvailable] = datatype;
				} else {
					// constant type handling
					if(purpose == SymbolPurpose.CONSTANT) {
						types[nextAvailable] = datatype;
					} else {
						types[nextAvailable] = DataType.UNDECTYPE;
					}
				}
				addresses[nextAvailable] = 0;
				index = nextAvailable;
				nextAvailable++;
			}
		}
		
		return index; // returns -1 if no space is available in table
		
	} // AddSymbol
	
	// Search through table and return index of matching symbol. Returns -1 if no matches found
	public int LookupSymbol(String symbol) {
		
		int index = -1; // index of existing symbol in table
		
		// check if the table is empty
		if(nextAvailable != 0) {
			// look for pre-existing symbols of the same name in the table
			for(int i = 0; i < nextAvailable; i++) {	
				// symbol matches existing token
				if(tokens[i].compareToIgnoreCase(symbol) == 0) {
					index = i;
				}
			}
		}
		
		return index; // returns -1 if symbol was not found in table
		
	} // LookupSymbol
	
	// looks at all stored symbols that are of an undeclared type and sets the appropriate type for the symbol
	public void UpdateTypes(DataType type) {
		for(int i = 0; i < nextAvailable; i++) {
			if(types[i] == DataType.UNDECTYPE && purposes[i] != SymbolPurpose.PROGRAM) {
				types[i] = type;
				
				if(type == DataType.LABELTYPE) {
					purposes[i] = SymbolPurpose.LABEL;
				}
			}
		}
	} // UpdateTypes
	
	// get the token for symbol at index
	public String GetToken(int index) {
		return tokens[index];
	} // GetToken
	
	// get the purpose for symbol at index
	public SymbolPurpose GetPurpose(int index) {
		return purposes[index];
	} // GetPurpose
	
	// get the type for symbol at index
	public DataType GetType(int index) {
		return types[index];
	} // GetType
	
	// return whether or not the declarations section has finished
	public boolean GetFinishDeclarations() {
		return finishDeclarations;
	}
	
	// set the purpose for symbol at index
	public void SetPurpose(int index, SymbolPurpose purpose) {
		purposes[index] = purpose;
	} // SetPurpose
	
	// set the type for a symbol at index
	public void SetType(int index, DataType type) {
		types[index] = type;
	} // SetType
	
	// sets a flag to indicate whether or not variable declarations have concluded
	public void SetFinishDeclarations() {
		finishDeclarations = true;
	} // SetFinishDeclarations
	
	// check if symbol stored at index is of type int
	public boolean isInteger(int index) {
		return types[index] == DataType.INTEGERTYPE;
	} // isInteger
	
	// check if symbol stored at index is of type float
	public boolean isFloat (int index) {
		return types[index] == DataType.FLOATTYPE;
	} // isFloat
	
	// check if symbol stored at index is of type string
	public boolean isString(int index) {
		return types[index] == DataType.STRINGTYPE;
	} // isString
	
	// check if symbol stored at index is of type label
	public boolean isLabel(int index) {
		return types[index] == DataType.LABELTYPE;
	} // isLabel
	
	// check if symbol stored at index is an undeclared type
	public boolean isUndeclared(int index) {
		return types[index] == DataType.UNDECTYPE;
	} // isUndeclared
	
	// return the address field stored at index
	public int GetAddress(int index) {
		return addresses[index];
	} // GetAddress
	
	// set the address field stored at index
	public void PutAddress(int index, int newAddress) {
		addresses[index] = newAddress;
	} // PutAddress
	
	// prints formatted SymbolTable to the specified file
	public void PrintSymbolTable(String filename) {
		
		// initialization phase
		int indexLength = 7; // minimum col spacing
		int longestTokenLength = 7;
		int longestPurposeLength = 11; // longest enum value is 8 characters
		int longestTypeLength = 13; // longest enum value is 10 characters
		//int longestAddressLength = 10;
		
		// if there are more than 10^7 entries in table, expand the col size of index
		if(Math.log(nextAvailable) > indexLength) {
			indexLength = ((int) Math.floor(Math.log(nextAvailable))) + 3;
		}
		
		// check for table entries with more characters than allotted for printing
		for(int i = 0; i < nextAvailable; i++) {
			
			// set token/address print sizes. + 3 for extra spacing
			int adjustedTokenLength = tokens[i].length() + 3;
			//int addressLength = ((int) Math.floor(Math.log(addresses[i])) + 3);
			
			// check current token/address length and compare against minimum or known longest
			if(adjustedTokenLength > longestTokenLength) { longestTokenLength = adjustedTokenLength; }
			//if(addressLength > longestAddressLength) { longestAddressLength = addressLength; }
			
		}
		
		// file writing error protection
		try {
			
			// open file and prepare output stream
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter); // included for printf support
			
			// table headers
			printWriter.printf("%-" + indexLength + "s%-" + longestTokenLength + "s%-" + longestPurposeLength + "s%-" + longestTypeLength + "s%s%n", "Index", "Token", "Purpose", "Type", "Address");
			
			// print all entries in quad table
			for(int i = 0; i < nextAvailable; i++) {
				printWriter.printf("%-" + indexLength + "d%-" + longestTokenLength + "s%-" + longestPurposeLength + "s%-" + longestTypeLength + "s%d%n", i, tokens[i], purposes[i], types[i], addresses[i]);
			}
			
			// close output streams and file
			printWriter.close();
			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
	} // PrintSymbolTable
	
} // SymbolTable
