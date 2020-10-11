package org.marioarias.monkey

import org.marioarias.monkey.repl.start

fun main() {
    println("Hello, this is the monkey.kt programming language")
    println("Feel free to type any command")
    start(System.`in`, System.out)
}