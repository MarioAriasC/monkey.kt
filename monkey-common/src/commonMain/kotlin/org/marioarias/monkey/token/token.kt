package org.marioarias.monkey.token


enum class TokenType(val value: String) {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),
    ASSIGN("="),
    EQ("=="),
    NOT_EQ("!="),
    IDENT("IDENT"),
    INT("INT"),
    PLUS("+"),
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),
    MINUS("-"),
    BANG("!"),
    SLASH("/"),
    ASTERISK("*"),
    LT("<"),
    GT(">"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN"),
    STRING("STRING");
}


fun String.lookupIdent(): TokenType = when (this) {
    "fn" -> TokenType.FUNCTION
    "let" -> TokenType.LET
    "true" -> TokenType.TRUE
    "false" -> TokenType.FALSE
    "if" -> TokenType.IF
    "else" -> TokenType.ELSE
    "return" -> TokenType.RETURN
    else -> TokenType.IDENT
}

data class Token(val type: TokenType, val literal: String) {
    constructor(type: TokenType, literal: Char) : this(type, literal.toString())
}