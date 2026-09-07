/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ADT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author abrouill
 * This class implements a file reading class, geared for reading lines of text
 * and buffering them for reading individual characters as needed for a 
 * lexical analyzer.  This is configured to count COMMENTS delimited by
 * { } or (* *) as whitespace and skip them completely.  Also prints an
 * error message if a comment is not terminated before EOF.  Returns '\n'
 * at the end of each line or when EOF is reached.
 * PUBLIC INTERFACE
 *   public LexScanner(String filename, boolean echoLines) 
 *     CONSTRUCTOR, opens filename as text, echolines TRUE causes each
 *     line of the file to be output to console, with a line number
 *  NOTE- IMMEDIATELY AFTER CALLING LEXSCANNER CONSTRUCTOR, CALL GETNEXTCHAR()
 *  TO PRIME THE PUMP AND GET THE FIRST CHARACTER OF THE FILE
 * 
 *  public boolean EOF() 
 *     returns true when the last character in the input file has been returned
 *     by GextNextChar
 * 
 *  public char PeekNextChar()
 *      returns the next character in the file WITHOUT advancing and removing
 *      the character from the input
 *
 * 
    public char GetNextChar() {
       Called to get the next character from file, automatically gets a new
       line when needed. CALL THIS TO GET CHARACTERS FOR GETIDENT etc.
       
    public boolean isWhitespace(char ch)
        SPACE, TAB, NEWLINE are white space
    
    public char skipWhiteSpace(char currCh) 
       Reads past all whitespace as defined by isWhiteSpace
       NOTE THAT COMMENTS ARE SKIPPED AS WHITESPACE AS WELL!
 
*/
public class LexScanner {

    private File file;                        //File to be read for input
    private FileReader filereader;            //Reader, Java reqd
    private BufferedReader bufferedreader;    //Buffered, Java reqd
    private String line;                      //Current line of input from file   
    private int linePos;                      //Current character position
    //  in the current line
    private boolean EOF;                      //End Of File indicator
    private boolean echo;                     //true means echo each input line
    private int lineCount;                    //line #in file, for echo-ing
    private boolean needLine;                 //track when to read a new line

    public LexScanner(String filename, boolean echoLines) {
        echo = echoLines;          //store echo status
        lineCount = 0;          //start the line number count
        line = "";              //line starts empty
        needLine = true;        //need to read a line
        linePos = -1;           //no chars read yet

        //set up the file access, get first character, line retrieved 1st time
        try {
            file = new File(filename);    //creates a new file instance  
            filereader = new FileReader(file);   //reads the file  
            bufferedreader = new BufferedReader(filereader);  //creates a buffering character input stream  
            EOF = false;
//            currCh = GetNextChar();
        } catch (IOException e) {
            EOF = true;
            e.printStackTrace();
        }

    }

    // Public access to the current End Of File status
    public boolean EOF() {
        return EOF;
    }

    // Returns the VALUE of the next character without removing it from the
    //    input line.  Useful for checking 2-character tokens that start with
    //    a 1-character token.
    public char PeekNextChar() {
        char result = ' ';
        if ((needLine) || (EOF)) {
            result = ' '; //at end of line, so nothing
        } else // 
        {
            if ((linePos + 1) < line.length()) { //have a char to peek
                result = line.charAt(linePos + 1);
            }
        }
        return result;
    }

   
    // Called to get the next character from file, automatically gets a new
    //      line when needed. CALL THIS TO GET CHARACTERS FOR GETIDENT etc.
    public char GetNextChar() {
        char result;
        if (needLine) //ran out last time we got a char, so get a new line
        {
            GetNextLine();
        }
        //try to get char from line buff
        if (EOF) {
            result = '\n';
            needLine = false;
        } else {
            if ((linePos < line.length() - 1)) { //have a character available
                linePos++;
                result = line.charAt(linePos);
            } else { //need a new line, but want to return eoln on this call first
                result = '\n';
                needLine = true; //will read a new line on next GetNextChar call
            }
        }
        return result;
    }

    // The constants below allow flexible comment start/end characters    
    final char commentStart_1 = '{';
    final char commentEnd_1 = '}';
    final char commentStart_2 = '(';
    final char commentPairChar = '*';
    final char commentEnd_2 = ')';

// Skips past single and multi-line comments, and outputs UNTERMINATED 
//  COMMENT when end of line is reached before terminating
    String unterminatedComment = "Comment not terminated before End Of File";

    private char skipComment(char curr) {
        if (curr == commentStart_1) {
            curr = GetNextChar();
            while ((curr != commentEnd_1) && (!EOF)) {
                curr = GetNextChar();
            }
            if (EOF) {
                myconsoleShowError(unterminatedComment);
            } else {
                curr = GetNextChar();
            }
        } else {
            if ((curr == commentStart_2) && (PeekNextChar() == commentPairChar)) {
                curr = GetNextChar(); // get the second
                curr = GetNextChar(); // into comment or end of comment
//            while ((curr != commentPairChar) && (PeekNextChar() != commentEnd_2) &&(!EOF)) {
                while ((!((curr == commentPairChar) && (PeekNextChar() == commentEnd_2))) && (!EOF)) {
//                if (lineCount >=4) {
                    //              System.out.println("In Comment, curr, peek: "+curr+", "+PeekNextChar());}
                    curr = GetNextChar();
                }
                if (EOF) {
                    myconsoleShowError(unterminatedComment);
                } else {
                    curr = GetNextChar();          //must move past close
                    curr = GetNextChar();          //must get following
                }
            }

        }
        return (curr);
    }

 // Called by GetNextChar when the cahracters in the current line are used up.
    // STUDENT CODE SHOULD NOT EVER CALL THIS!
    private void GetNextLine() {
        try {
            line = bufferedreader.readLine();
            if ((line != null) && (echo)) {
                lineCount++;
                System.out.println(String.format("%04d", lineCount) + " " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line == null) {    // The readLine returns null at EOF, set flag
            EOF = true;
        }
        linePos = -1;      // reset vars for new line if we have one
        needLine = false;  // we have one, no need
        //the line is ready for the next call to get a character
    }

    // Category for any whitespace to be skipped over
    public boolean isWhitespace(char ch) {
        // SPACE, TAB, NEWLINE are white space
        return ((ch == ' ') || (ch == '\t') || (ch == '\n'));
    }
    // Reads past all whitespace as defined by isWhiteSpace
    // NOTE THAT COMMENTS ARE SKIPPED AS WHITESPACE AS WELL!

    public char skipWhiteSpace(char currCh) {

        do {
            while ((isWhitespace(currCh)) && (!EOF)) {
                currCh = GetNextChar();
            }
            currCh = skipComment(currCh);
        } while (isWhitespace(currCh) && (!EOF));
        return currCh;
    }

    private void myconsoleShowError(String message) {
        System.out.println("**** ERROR FOUND: " + message);
    }
    // Character category for alphabetic chars

}
