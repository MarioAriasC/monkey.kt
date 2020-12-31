package org.marioarias.monkey.repl

import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.parser.Parser
import java.io.InputStream
import java.io.PrintStream
import java.util.*

const val MONKEY_FACE = """            __,__
   .--.  .-"     "-.  .--.
  / .. \/  .-. .-.  \/ .. \
 | |  '|  /   Y   \  |'  | |
 | \   \  \ 0 | 0 /  /   / |
  \ '- ,\.-""${'"'}${'"'}${'"'}${'"'}${'"'}-./, -' /
   ''-' /_   ^ ^   _\ '-''
       |  \._   _./  |
       \   \ '~' /   /
        '._ '-=-' _.'
           '-----'
"""

fun start(`in`: InputStream, out: PrintStream) {
    val scanner = Scanner(`in`)
    while (scanner.hasNext()) {
        val code = scanner.nextLine()
        val lexer = Lexer(code)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        if (parser.errors().isNotEmpty()) {
            printParserErrors(out, parser.errors())
            continue
        }

        out.println(program.toString())
    }
}

fun printParserErrors(out: PrintStream, errors: List<String>) {
    out.println(MONKEY_FACE)
    out.println("Woops! we ran into some monkey business here!")
    out.println(" parser errors:")
    errors.forEach { error ->
        out.println("\t$error")
    }
}