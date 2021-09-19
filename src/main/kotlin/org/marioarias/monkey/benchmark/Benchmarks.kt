package org.marioarias.monkey.benchmark

import org.marioarias.monkey.ast.Program
import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.evaluator.Environment
import org.marioarias.monkey.evaluator.Evaluator.eval
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.parser.Parser
import org.marioarias.monkey.vm.VM
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
object Benchmarks {
    private const val input = """
let fibonacci = fn(x) {    
	if (x == 0) {
		return 0;	
	} else {
		if (x == 1) {
			return 1;
		} else {
			fibonacci(x - 1) + fibonacci(x - 2);
		}
	}
};
fibonacci(35);        
    """

    private fun parse(): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }


    private fun measure(engine: String, body: () -> MObject) {
        val result = measureTimedValue(body)
        println("engine=$engine, result=${result.value.inspect()}, duration=${result.duration}")
    }

    fun vm() {
        val compiler = MCompiler()
        compiler.compile(parse())
        val machine = VM(compiler.bytecode())
        measure("vm") {
            machine.run()
            machine.lastPoppedStackElem()!!
        }
    }

    fun eval() {
        val end = Environment.newEnvironment()
        measure("eval") {
            eval(parse(), end)!!
        }
    }

    fun kotlin() {
        measure("kotlin") {
            fibonacci()
        }
    }

    private fun fibonacci(): MInteger {
        fun step(x: Long): Long {
            return when (x) {
                0L -> 0L
                1L -> 1L
                else -> step(x - 1) + step(x - 2)
            }
        }
        return MInteger(step(35))
    }
}