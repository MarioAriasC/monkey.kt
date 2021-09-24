package org.marioarias.monkey

import org.marioarias.monkey.benchmark.Benchmarks
import org.marioarias.monkey.repl.start

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Hello, this is the monkey.kt programming language")
        println("Feel free to type any command")
        start(System.`in`, System.out)
    } else {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.fastInput)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.fastInput)
            "kotlin" -> Benchmarks.kotlin()
        }
    }
}