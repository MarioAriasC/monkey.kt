package org.marioarias.monkey.repl

import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.compiler.MCompilerException
import org.marioarias.monkey.evaluator.Environment
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.parser.Parser
import org.marioarias.monkey.vm.VM
import org.marioarias.monkey.vm.VMException
import java.io.InputStream
import java.io.PrintStream
import java.util.*

const val MONKEY_FACE = """            __,__
   .--.  .-"     "-.  .--.
  / .. \/  .-. .-.  \/ .. \                             '
 | |  '|  /   Y   \  |'  | |
 | \   \  \ 0 | 0 /  /   / |
  \ '- ,\.-""${'"'}${'"'}${'"'}${'"'}${'"'}-./, -' /
   ''-' /_   ^ ^   _\ '-''
       |  \._   _./  |
       \   \ '~' /   /
        '._ '-=-' _.'
           '-----'
"""

const val PROMPT = ">>>"

fun start(`in`: InputStream, out: PrintStream) {
    val scanner = Scanner(`in`)
    out.print("$PROMPT ")
//    val env = Environment.newEnvironment()
//    val macroEnv = Environment.newEnvironment()
    while (scanner.hasNext()) {

        val code = scanner.nextLine()
        val lexer = Lexer(code)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        if (parser.errors().isNotEmpty()) {
            printParserErrors(out, parser.errors())
            continue
        }
        /*val macroProgram = defineMacros(program, macroEnv)
        val expanded = expandMacros(macroProgram, macroEnv)
        val evaluated = Evaluator.eval(expanded, env)

        if (evaluated != null) {
            out.println(evaluated.inspect())
        }*/

        try {
            val compiler = MCompiler()
            compiler.compile(program)
            val machine = VM(compiler.bytecode())
            machine.run()
            val stackTop = machine.stackTop()
            out.println(stackTop?.inspect())
        } catch (e: MCompilerException) {
            out.println("Woops! Compilation failed:\n ${e.message}")
            out.print("$PROMPT ")
            continue
        } catch (e: VMException) {
            out.println("Woops! Execution bytecode failed:\n ${e.message}")
            out.print("$PROMPT ")
            continue
        }
        
        out.print("$PROMPT ")
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