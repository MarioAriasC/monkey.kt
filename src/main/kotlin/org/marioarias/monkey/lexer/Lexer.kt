package org.marioarias.monkey.lexer

import org.marioarias.monkey.token.Token
import org.marioarias.monkey.token.TokenType
import org.marioarias.monkey.token.TokenType.*
import org.marioarias.monkey.token.lookupIdent


class Lexer(private val input: String) {
    private var position = 0
    private var readPosition = 0
    private var ch = 0.toChar()

    init {
        readChar()
    }

    private fun readChar() {
        ch = peakChar()
        position = readPosition
        readPosition++
    }

    private fun peakChar(): Char {
        return if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
    }

    private fun readNumber(): String = readValue { ch -> ch.isDigit() }

    private fun readIdentifier(): String = readValue { ch -> ch.isIdentifier() }

    private fun readValue(predicate: (Char) -> Boolean): String {
        val currentPosition = position
        while (predicate(ch)) {
            readChar()
        }
        return input.substring(currentPosition, position)
    }

    private fun readString(): String {
        val start = position + 1
        while (true) {
            readChar()
            if (ch == '"' || ch == 0.toChar()) {
                break
            }
        }
        return input.substring(start, position)
    }

    fun nextToken(): Token {

        fun endsWithEqual(oneChar: TokenType, twoChars: TokenType, duplicateChars: Boolean = true) = if (peakChar() == '=') {
            val currentChar = ch
            readChar()
            val value = if (duplicateChars) {
                "$currentChar$currentChar"
            } else {
                "$currentChar$ch"
            }
            Token(twoChars, value)
        } else {
            oneChar.token()
        }

        skipWhitespace()
        var readNextChar = true
        return when (ch) {
            '=' -> endsWithEqual(ASSIGN, EQ)
            ';' -> SEMICOLON.token()
            ':' -> COLON.token()
            ',' -> COMMA.token()
            '(' -> LPAREN.token()
            ')' -> RPAREN.token()
            '{' -> LBRACE.token()
            '}' -> RBRACE.token()
            '[' -> LBRACKET.token()
            ']' -> RBRACKET.token()
            '+' -> PLUS.token()
            '-' -> MINUS.token()
            '*' -> ASTERISK.token()
            '/' -> SLASH.token()
            '<' -> LT.token()
            '>' -> GT.token()
            '!' -> endsWithEqual(BANG, NOT_EQ, duplicateChars = false)
            '"' -> Token(STRING, readString())
            0.toChar() -> Token(EOF, "")
            else -> {
                when {
                    ch.isIdentifier() -> {
                        val identifier = readIdentifier()
                        readNextChar = false
                        Token(identifier.lookupIdent(), identifier)
                    }
                    ch.isDigit() -> {
                        readNextChar = false
                        Token(INT, readNumber())
                    }
                    else -> {
                        Token(ILLEGAL, ch.toString())
                    }
                }
            }
        }.also {
            if (readNextChar) {
                readChar()
            }
        }
    }


    private fun TokenType.token() = Token(this, ch)

    private fun skipWhitespace() {
        while (whiteSpaces.contains(ch)) {
            readChar()
        }
    }


    override fun toString(): String {
        return "Lexer(input='$input', position=$position, readPosition=$readPosition, ch=$ch)"
    }

    private fun Char.isIdentifier() = this.isLetter() || this == '_'

    companion object {
        val whiteSpaces = listOf(' ', '\t', '\n', '\r')
    }


}