//package project3;

import ADT.*;

/**
 *
 * @author lmorsman SPRING 2026
 */

/*
 * ALL CFGs FOR SYNTAX ANALYZER
 * 
 * <program> -> PROGRAM <progidentifier> $SEMICOLON <block> $PERIOD
 * <block> -> <variable-dec-sec> <block-body>
 * <block-body> -> $BEGIN <statementlist> $END
 * <statementlist> -> <statement> | <statement> $SCOLN <statementlist>
 * <variable-dec-sec> -> {<variable-declaration>}*
 * <variable-declaration> -> <identifierlist> $COLON <simple type> $SEMICOLON
 * <identifierlist> -> <identifier> | <identifier> $COMMA <identifierlist>
 * <statement>->
[<labelidentifier> $COLON]*
(
<variable> $ASSIGN
(<simple expression> | <string literal>) |
<block-body> |
$IF <relexpression> $THEN <statement>
[$ELSE <statement>] |
$WHILE <relexpression> DO <statement> |
$REPEAT <statementlist> $UNTIL <relexpression> |
$FOR <variable> $ASSIGN <simple expression>
$TO <simple expression> $DO <statement> |
$GOTO <labelidentifier>|
$WRITELN $LPAR (<simple expression> | <identifier> |
<stringconst> ) $RPAR|
$READLN $LPAR <identifier> $RPAR
)

 *<variable> -> <identifier>
 *<labelidentifier> -> <identifier> 
 *<progidentifier> -> <identifier> 
 *<relexpression> -> <simple expression> <relop> <simple expression>
 *<relop> -> $EQ | $LSS | $GTR | $NEQ | $LEQ | $GEQ
 *<simple expression>-> [<sign>] <term> | [<sign>] <term> <addop>
 *<addop> -> $PLUS | $MINUS
 *<sign> -> $PLUS | $MINUS
 *<term> -> <factor> | <factor> <mulop> <term>
 *<mulop> -> $MULT | $DIVIDE
 *<factor> -> <unsigned constant> | <variable> | $LPAR <simple expression> $RPAR
 *<simple type> -> $INTEGER | $FLOAT | $STRING | $LABEL
 *<constant> -> [<sign>] <unsigned constant>
 *<unsigned constant>-> <unsigned number>
 *<unsigned number>-> $FLOATTYPE | $INTTYPE Token codes 52 or 51
 *<identifier> -> $IDENTIFIER Token code 50
**note: <letter> {<letter> |<digit> | _ _ }*
 *<stringconst> -> $STRINGTYPE Token code 53
 */
public class Main {

    public static void main(String[] args) {
       String filePath = args[0];
       boolean traceon = false;
       System.out.println("Logan Morsman, 8872, CS4100/5100, SPRING 2026");
       System.out.println("INPUT FILE TO PROCESS IS: " + filePath);
    
       Syntactic parser = new Syntactic(filePath, traceon);
       parser.parse();
       System.out.println("Done.");
    } // main

} // Main
