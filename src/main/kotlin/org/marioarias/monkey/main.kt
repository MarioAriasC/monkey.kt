package org.marioarias.monkey

import org.marioarias.monkey.benchmark.Bencharmarks
import org.marioarias.monkey.repl.start

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Hello, this is the monkey.kt programming language")
        println("Feel free to type any command")
        start(System.`in`, System.out)
    } else {
        when (args.first()) {
            "vm" -> Bencharmarks.vm()
            "eval" -> Bencharmarks.eval()
        }
    }
}