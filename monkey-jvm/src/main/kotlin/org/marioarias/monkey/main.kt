package org.marioarias.monkey

import org.marioarias.monkey.benchmark.Benchmarks
import org.marioarias.monkey.repl.start

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Hello, this is the monkey.kt programming language")
        println("JVM implementation " + System.getProperty("java.version"))
        println("Feel free to type any command")
        start(::readLine, ::println)
    } else {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.fastInput)
            "vm-fast2" -> Benchmarks.vm(Benchmarks.fastInput2)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.fastInput)
            "eval-fast2" -> Benchmarks.eval(Benchmarks.fastInput2)
            "kotlin" -> Benchmarks.kotlin()
        }
    }
}