package com.programming.luxembourg;

import com.programming.luxembourg.Types.TokenType;

public class Token {
    final TokenType type;
    final String lexme;
    final Object literal;
    final int line;

    public Token(TokenType type, String lexme, Object literal, int line){

        this.type = type;
        this.lexme = lexme;
        this.literal = literal;
        this.line = line;
    }
    public String toString(){
        return type+" "+lexme+" "+literal;

    }

}
