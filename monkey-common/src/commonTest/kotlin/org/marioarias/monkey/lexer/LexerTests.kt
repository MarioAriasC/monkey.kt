package org.marioarias.monkey.lexer

import org.marioarias.monkey.token.TokenType
import org.marioarias.monkey.token.TokenType.*
import kotlin.test.Test

import kotlin.test.assertEquals


class LexerTests {
    @Test
    fun `validate lexer`() {
        val code = """
let five = 5;
let ten = 10;

let add = fn(x, y) {
	x + y;
}

let result = add(five, ten);
!-/*5;
5 < 10 > 5;

if (5 < 10) {
	return true;
} else {
	return false;
}

10 == 10;
10 != 9;   
"foobar"
"foo bar"     
[1,2];
{"foo":"bar"}
         """.trimIndent()

        val lexer = Lexer(code)

        val expected = listOf(
            LET to "let",
            IDENT to "five",
            ASSIGN to "=",
            INT to "5",
            SEMICOLON to ";",
            LET to "let",
            IDENT to "ten",
            ASSIGN to "=",
            INT to "10",
            SEMICOLON to ";",
            LET to "let",
            IDENT to "add",
            ASSIGN to "=",
            FUNCTION to "fn",
            LPAREN to "(",
            IDENT to "x",
            COMMA to ",",
            IDENT to "y",
            RPAREN to ")",
            LBRACE to "{",
            IDENT to "x",
            PLUS to "+",
            IDENT to "y",
            SEMICOLON to ";",
            RBRACE to "}",
            LET to "let",
            IDENT to "result",
            ASSIGN to "=",
            IDENT to "add",
            LPAREN to "(",
            IDENT to "five",
            COMMA to ",",
            IDENT to "ten",
            RPAREN to ")",
            SEMICOLON to ";",
            BANG to "!",
            MINUS to "-",
            SLASH to "/",
            ASTERISK to "*",
            INT to "5",
            SEMICOLON to ";",
            INT to "5",
            LT to "<",
            INT to "10",
            GT to ">",
            INT to "5",
            SEMICOLON to ";",
            IF to "if",
            LPAREN to "(",
            INT to "5",
            LT to "<",
            INT to "10",
            RPAREN to ")",
            LBRACE to "{",
            RETURN to "return",
            TRUE to "true",
            SEMICOLON to ";",
            RBRACE to "}",
            ELSE to "else",
            LBRACE to "{",
            RETURN to "return",
            FALSE to "false",
            SEMICOLON to ";",
            RBRACE to "}",
            INT to "10",
            EQ to "==",
            INT to "10",
            SEMICOLON to ";",
            INT to "10",
            NOT_EQ to "!=",
            INT to "9",
            SEMICOLON to ";",
            STRING to "foobar",
            STRING to "foo bar",
            LBRACKET to "[",
            INT to "1",
            COMMA to ",",
            INT to "2",
            RBRACKET to "]",
            SEMICOLON to ";",
            LBRACE to "{",
            STRING to "foo",
            COLON to ":",
            STRING to "bar",
            RBRACE to "}",
            EOF to ""
        )

        expected.forEach { (type, literal) ->
            val token = lexer.nextToken()
            assertEquals(token.type, type)
            assertEquals(token.literal, literal)
        }
    }


}