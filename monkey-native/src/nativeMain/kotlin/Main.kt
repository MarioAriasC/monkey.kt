import org.marioarias.monkey.benchmark.Benchmarks
import org.marioarias.monkey.repl.start

fun main(args: Array<String>) {

    if (args.isEmpty()) {
        println("Hello, this is the monkey.kt programming language")
        println("Kotlin Native Implementation")
        println("Feel free to type any command")
        start(::readlnOrNull, ::println)
    } else {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
            "kotlin" -> Benchmarks.kotlin()
        }
    }
}