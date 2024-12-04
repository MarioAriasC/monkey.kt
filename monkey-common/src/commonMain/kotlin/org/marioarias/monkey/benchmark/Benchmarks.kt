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
    private const val SLOW_INPUT = """
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

    const val FAST_INPUT = """
let fibonacci = fn(x) {    	
    if (x < 2) {
    	return x;
    } else {
    	fibonacci(x - 1) + fibonacci(x - 2);
    }
};
fibonacci(35);        
    """

    const val FAST_INPUT_2 = """
let fibonacci = fn(x) {    	
    if (x < 2) {
    	return x;
    }
    fibonacci(x - 1) + fibonacci(x - 2);    
};
fibonacci(35);        
    """

    const val CACHED = """
    let fibRec = fn(n, buf) {
      if(n > 2) {
        let res = buf[0] + buf[1];
        return fibRec(n - 1, [res, buf[0]]);  
      }
      return buf;
    }                        
                            
                            
    let fibonacci = fn(x) {
      let res = [1,1]
      return fibRec(x, res)[0]
    }
    
    fibonacci(35);"""

    private fun parse(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }


    private fun measure(engine: String, body: () -> MObject) {
        val result = measureTimedValue(body)
        println("engine=$engine, result=${result.value.inspect()}, duration=${result.duration}")
    }

    fun vm(input: String = this.SLOW_INPUT) {
        val compiler = MCompiler()
        compiler.compile(parse(input))
        val machine = VM(compiler.bytecode())
        measure("vm") {
            machine.run()
            machine.lastPoppedStackElem()!!
        }
    }


    fun eval(input: String = this.SLOW_INPUT) {
        val end = Environment.newEnvironment()
        measure("eval") {
            eval(parse(input), end)!!
        }
    }

    fun kotlin() {
        measure("kotlin") {
            fibonacci()
        }
    }

    fun kotlinRec() {
        measure("kotlin") {
            fibonacci2()
        }
    }

    private fun fibonacci(): MInteger {
        fun step(x: Long): Long {
//            println(x)
            return when (x) {
                0L -> 0L
                1L -> 1L
                else -> step(x - 1) + step(x - 2)
            }
        }
        return MInteger(step(35))
    }

    private fun fibonacci2(): MInteger {
        fun stepRec(x: Long, buf: LongArray): LongArray {
//            print(x)
//            println(buf.toList())
            if (x > 2) {
                val res = buf[0] + buf[1]
                return stepRec(x - 1, longArrayOf(res, buf[0]))
            }
            return buf
        }


        fun step(x: Long): Long {
            return stepRec(x, longArrayOf(1, 1))[0]
        }

        return MInteger(step(35))
    }
}