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

    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN"),
    STRING("STRING")
    ;

    companion object {
        val keyboards = mapOf(
            "fn" to FUNCTION,
            "let" to LET,
            "true" to TRUE,
            "false" to FALSE,
            "if" to IF,
            "else" to ELSE,
            "return" to RETURN
        )
    }
}

fun String.lookupIdent(): TokenType {
    return TokenType.keyboards[this] ?: TokenType.IDENT
}

data class Token(val type: TokenType, val literal: String) {
    constructor(type: TokenType, literal: Char) : this(type, literal.toString())
}