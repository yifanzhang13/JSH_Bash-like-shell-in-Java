package uk.ac.ucl.jsh.core.parser;
import java_cup.runtime.*;

import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;

%%
%class Lexer
%unicode
%public
%implements java_cup.runtime.Scanner
%type void
%function yylex
%eofval{
     if(stack.size() > 0){
        throw new IllegalArgumentException();
     }
     pushAtomEnd();
     symbolQueue.add(symbol(sym.EOF));
     return;
%eofval}
%eofclose
%line
%column

%{

    private Queue<java_cup.runtime.Symbol> symbolQueue = new LinkedList<>();
    private Stack<Integer> stack = new Stack<>();
    private Boolean currentlyAtom = false;
    private StringBuffer string = new StringBuffer();

    public java_cup.runtime.Symbol next_token() throws java.io.IOException {
        if(symbolQueue.size() <= 0){
            yylex();
        }
        return symbolQueue.remove();
    }

    private Symbol symbol(int type){
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }

    private void yyPushState(int newState) {
        stack.push(yystate());
        yybegin(newState);
    }
    private void yyPopState()   {
        yybegin(stack.pop());
    }

    private void pushAtomEnd(){
        if(currentlyAtom){
            symbolQueue.add(symbol(sym.ATOMEND, string.toString()));
            string.setLength(0);
            currentlyAtom = false;
        }
    }

    private void pushAtom(){
        if(currentlyAtom){
            symbolQueue.add(symbol(sym.ATOM, string.toString()));
            string.setLength(0);
        }
    }

%}

Whitespace = [\ \t\f]
ReturnChars = [\r\n]
Letter = [a-zA-Z]
Digit = [0-9]
Punctuation = [!#$%&\"'()*+,-./:;<=>?@\[\\\]\^_`{¦}~|]
Punct_NoSp = [!#$%&\()*+,-./:=?@\[\\\]\^_{¦}~]

CharValues = {Letter}|{Digit}|{Punctuation}|{Whitespace}
AtomValues = {Letter}|{Digit}|{Punct_NoSp}

%state YYDOUBLEQUOTE, YYSINGLEQUOTE, YYBACKQUOTE
%%

<YYINITIAL>{

    \'              {   yyPushState(YYSINGLEQUOTE);
                        symbolQueue.add(symbol(sym.SQUOTE));
                    }
    \"              {   yyPushState(YYDOUBLEQUOTE);
                        symbolQueue.add(symbol(sym.DQUOTE));
                    }

    \`              {
                        yyPushState(YYBACKQUOTE);
                        pushAtom();
                    }
    ">"             {
                        pushAtomEnd();
                        symbolQueue.add(symbol(sym.GREATER));
                        return;
                    }
    "<"             {   pushAtomEnd();
                        symbolQueue.add(symbol(sym.LESSER));
                        return;
                    }
    "|"             {   pushAtomEnd();
                        symbolQueue.add(symbol(sym.PIPE));
                        return;
                    }
    ";"             {   pushAtomEnd();
                        symbolQueue.add(symbol(sym.SEMI));
                        return;
                    }
    {ReturnChars}   {
                        throw new IllegalArgumentException();
                    }
    {Whitespace}+   {   pushAtomEnd();
                    }

    {AtomValues}    {   string.append(yytext());
                        currentlyAtom = true;
                    }
}


<YYDOUBLEQUOTE>{
    \"              {   yyPopState();
                    }
    \`              { yyPushState(YYBACKQUOTE);
                        pushAtom();
                      return;
                    }
    {ReturnChars}   { throw new IllegalArgumentException();                 }
    {CharValues}    { string.append(yytext());
                      currentlyAtom = true;
                    }
}

<YYSINGLEQUOTE>{
    \'              { yyPopState();                                         }
    {ReturnChars}   { throw new IllegalArgumentException();                 }
    {CharValues}    { string.append(yytext());
                      currentlyAtom = true; }
}

<YYBACKQUOTE>{
    \`              {   yyPopState();
                        if(string.length() > 0){
                            symbolQueue.add(symbol(sym.BACKQUOTE, string.toString()));
                            string.setLength(0);
                        }
                        return;
                    }
    {ReturnChars}   { throw new IllegalArgumentException();                 }
    {CharValues}    { string.append(yytext());
                        currentlyAtom = true; }
}


[^] {
    System.out.println((yyline+1)+" Error: Bad Character at '" + yytext() + "'");
}