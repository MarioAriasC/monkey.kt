package org.marioarias.monkey.repl

import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.token.TokenType
import java.io.InputStream
import java.io.PrintStream
import java.util.*

fun start(`in`: InputStream, out: PrintStream) {
    val scanner = Scanner(`in`)
    while (scanner.hasNext()) {
        val code = scanner.nextLine()
        val lexer = Lexer(code)
        end@ while (true) {
            val token = lexer.nextToken()
            if (token.type != TokenType.EOF) {
                out.println(token)
            } else {
                break@end
            }
        }
    }
}