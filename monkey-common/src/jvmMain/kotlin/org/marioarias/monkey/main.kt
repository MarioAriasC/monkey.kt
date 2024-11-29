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
            "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
            "vm-fast2" -> Benchmarks.vm(Benchmarks.FAST_INPUT_2)
            "vm-cached" -> Benchmarks.vm(Benchmarks.CACHED)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
            "eval-fast2" -> Benchmarks.eval(Benchmarks.FAST_INPUT_2)
            "eval-cached" -> Benchmarks.eval(Benchmarks.CACHED)
            "kotlin" -> Benchmarks.kotlin()
            "kotlin-cached" -> Benchmarks.kotlinRec()
        }
    }
}