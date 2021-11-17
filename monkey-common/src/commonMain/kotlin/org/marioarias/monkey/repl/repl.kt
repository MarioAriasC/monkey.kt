package org.marioarias.monkey.repl

import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.compiler.MCompilerException
import org.marioarias.monkey.compiler.SymbolTable
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.builtins
import org.marioarias.monkey.parser.Parser
import org.marioarias.monkey.vm.VM
import org.marioarias.monkey.vm.VMException


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

const val PROMPT = ">>>"

fun start(readLine: () -> String?, write: (String?) -> Unit) {
    var constants = mutableListOf<MObject>()
    val globals = mutableListOf<MObject>()
    val symbolTable = SymbolTable()
    builtins.forEachIndexed { i, (name, _) ->
        symbolTable.defineBuiltin(i, name)
    }
    while (true) {
        write("$PROMPT ")
        val code = readLine().takeIf { it != "" } ?: return
        val lexer = Lexer(code)
        val parser = Parser(lexer)
        val program = parser.parseProgram()

        if (parser.errors().isNotEmpty()) {
            printParserErrors(write, parser.errors())
            continue
        }

        try {
            val compiler = MCompiler(constants, symbolTable)
            compiler.compile(program)
            val bytecode = compiler.bytecode()
            constants = bytecode.constants.toMutableList()
            val machine = VM(bytecode, globals)
            machine.run()
            val stackTop = machine.lastPoppedStackElem()
            write(stackTop?.inspect())
        } catch (e: MCompilerException) {
            write("Woops! Compilation failed:\n ${e.message}")
            write("$PROMPT ")
            continue
        } catch (e: VMException) {
            write("Woops! Execution bytecode failed:\n ${e.message}")
            write("$PROMPT ")
            continue
        }
    }
}

fun printParserErrors(write: (String) -> Unit, errors: List<String>) {
    write(MONKEY_FACE)
    write("Woops! we ran into some monkey business here!")
    write(" parser errors:")
    errors.forEach { error ->
        write("\t$error")
    }
}