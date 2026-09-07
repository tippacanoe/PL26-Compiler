package ADT;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DataMem {
	
	private int maxSize; // maximum rows in table
	private int nextAvailable; // next open row in table
	private VariantData data[]; // table of data
	
	// constructor
	public DataMem(int maxSize) {
		this.maxSize = maxSize;
		nextAvailable = 0;
		data = new VariantData[this.maxSize];
	} // constructor
	
	// adds a new value to the data list. Verifies there is space available before storing data
	public int Malloc(String value) {
		int storedIndex = -1;
		if(nextAvailable < maxSize) { 
			data[nextAvailable] = new VariantData(value);
			storedIndex = nextAvailable;
			nextAvailable++;
		}
		return storedIndex;
	} // Malloc
	
	// adds a new value to the data list. Verifies there is space available before storing data
	public int Malloc(int value) {
		int storedIndex = -1;
		if(nextAvailable < maxSize) { 
			data[nextAvailable] = new VariantData(value);
			storedIndex = nextAvailable;
			nextAvailable++;
		}
		return storedIndex;
	} // Malloc
	
	// adds a new value to the data list. Verifies there is space available before storing data
	public int Malloc(double value) {
		int storedIndex = -1;
		if(nextAvailable < maxSize) { 
			data[nextAvailable] = new VariantData(value);
			storedIndex = nextAvailable;
			nextAvailable++;
		}
		return storedIndex;
	} // Malloc
	
	// replace value in existing data slot. This will do nothing if the new value does not match the data type stored at the passed index
	public int put(int value, int index) {
		
		// initialization phase. returns 0 if new data matches original data type. Returns -1 if types do not match
		int typeMatch = 0;
		
		// verify new data type is matching existing data type
		if(isInteger(index)) {
			data[index].setInt(value);
		} else {
			typeMatch = -1;
		}
		
		return typeMatch;
		
	} // put
	
	// replace value in existing data slot. This will do nothing if the new value does not match the data type stored at the passed index
	public int put(double value, int index) {
		
		// initialization phase. returns 0 if new data matches original data type. Returns -1 if types do not match
		int typeMatch = 0;
		
		// verify new data type is matching existing data type
		if(isFloat(index)) {
			data[index].setFloat(value);
		} else {
			typeMatch = -1;
		}
		
		return typeMatch;
		
	} // put
	
	// replace value in existing data slot. This will do nothing if the new value does not match the data type stored at the passed index
	public int put(String value, int index) {
		
		// initialization phase. returns 0 if new data matches original data type. Returns -1 if types do not match
		int typeMatch = 0;
		
		// verify new data type is matching existing data type
		if(isString(index)) {
			data[index].setString(value);
		} else {
			typeMatch = -1;
		}
		
		return typeMatch;
		
	} // put
	
	// return data stored at index if it is an int, otherwise return max int value
	public int getInt(int index) {
		int value = Integer.MAX_VALUE;
		if(isInteger(index)) { value = data[index].getInt(); } 
		return value;
	} // getInt
	
	// return data stored at index if it is a float, otherwise return NaN
	public double getFloat(int index) {
		double value = Double.NaN;
		if(isFloat(index)) { value = data[index].getFloat(); } 
		return value;
	} // getFloat
	
	// return data stored at index if it is a string, otherwise return null
	public String getString(int index) {
		String value = null;
		if(isString(index)) { value = data[index].getString(); }
		return value;
	} // getString
	
	// check if data stored at index is of type integer
	public boolean isInteger(int index) {
		return data[index].getType() == DataType.INTEGERTYPE;
	} // isInteger
	
	// check if data stored at index is of type float/double
	public boolean isFloat(int index) {
		return data[index].getType() == DataType.FLOATTYPE;
	} // isFloat
	
	// check if data stored at index is of type string
	public boolean isString(int index) {
		return data[index].getType() == DataType.STRINGTYPE;
	} // isString
	
	public boolean isLabel(int index) {
		return data[index].getType() == DataType.LABELTYPE;
	} // isLabel
	
	public void PrintDataMem(String filename) {
		// initialization phase
		int indexLength = 7; // minimum index col spacing
		int longestDataLength = indexLength; // minimum data col spacing
		
		// if there are more than 10^10 entries in table, expand the col size of index
		if(Math.log(nextAvailable) > indexLength) {
			indexLength = ((int) Math.floor(Math.log(nextAvailable)));
		}
	
		// find the entry in table that uses the most amount of characters, and expand column spacing if needed
		if(nextAvailable > 1) {
			for(int i = 1; i < nextAvailable; i++) {
				// check if integer uses more digits than longest known length
				if(isInteger(i) && (Math.log(data[i].getInt()) > longestDataLength)) {
					longestDataLength = ((int) Math.floor(Math.log(data[i].getInt())));
				} 
				
				// check if float uses more digits than longest known length. this may not work as expected right now
				else if (isFloat(i) && (Math.log(data[i].getFloat()) > longestDataLength)) {
					longestDataLength = ((int) Math.floor(Math.log(data[i].getFloat())));
				} 
				
				// check if string uses more characters than longest known length
				else if (isString(i) && (data[i].getString().length() > longestDataLength)) {
					longestDataLength = data[i].getString().length();
				}
			}
		}
		
		// add additional col spacing
		indexLength += 3;
		longestDataLength += 3;
		
		// file writing error protection
		try {
			
			// open file and prepare output stream
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			PrintWriter printWriter = new PrintWriter(bufferedWriter); // included for printf support
			
			// table headers
			printWriter.printf("%-" + indexLength + "s%-" + longestDataLength + "s%n", "Index", "Data");
		
			// print all entries in DataMem table
			for(int i = 0; i < nextAvailable; i++) {
				// data type matters for printf, so the conditionals here check which data type we are attempting to print out before printing
				if(isInteger(i)) {
					printWriter.printf("%-" + indexLength + "d%-" + longestDataLength + "d%n", i, data[i].getInt());
				} else if (isFloat(i)) {
					printWriter.printf("%-" + indexLength + "d%-" + longestDataLength + "f%n", i, data[i].getFloat());
				} else {
					printWriter.printf("%-" + indexLength + "d%-" + longestDataLength + "s%n", i, data[i].getString());
				}
				
			}
			
			// close output streams and file
			printWriter.close();
			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
	} // PrintDataMem
	
	// data structure that acts as a row of data for the DataMem list. 
	class VariantData {
		
		// different available data types. only one can be set per object due to restrictions placed based on type
		private int integerValue;
		private double doubleValue;
		private String stringValue;
		private DataType type;
		
		// overload constructor to store one int
		VariantData(int value) {
			type = DataType.INTEGERTYPE;
			integerValue = value;
		} // constructor
		
		// overload constructor to store one double
		VariantData(double value) {
			type = DataType.FLOATTYPE;
			doubleValue = value;
		} // constructor
		
		// overload constructor to store one string
		VariantData(String value) {
			type = DataType.STRINGTYPE;
			stringValue = value;
		} // constructor
		
		// returns the stored data type for this data
		DataType getType() {
			return type;
		} // getType
		
		int getInt() {
			return integerValue;
		} // getInt
		
		double getFloat() {
			return doubleValue;
		} // getFloat
		
		String getString() {
			return stringValue;
		} // getString
		
		// set the passed value in data only if the data types match
		void setInt(int value) {
			if(getType() == DataType.INTEGERTYPE) {
				integerValue = value;
			}
		} // setInt
		
		// set the passed value in data only if the data types match
		void setFloat(double value) {
			if(getType() == DataType.FLOATTYPE) {
				doubleValue = value;
			}
		} // setFloat
		
		// set the passed value in data only if the data types match
		void setString(String value) {
			if(getType() == DataType.STRINGTYPE) {
				stringValue = value;
			}
		} // setString
		
	} // VariantData
	
} // DataMem
