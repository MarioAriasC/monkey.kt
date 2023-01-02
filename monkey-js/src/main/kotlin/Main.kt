import org.marioarias.monkey.benchmark.Benchmarks

fun main() {
    Benchmarks.vm(Benchmarks.fastInput)
    /*if (args.isEmpty()) {
        println("Hello, this is the monkey.kt programming language")
        println("Kotlin JS implementation")
        println("REPL isn't implemented for JS")
    } else {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.fastInput)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.fastInput)
            "kotlin" -> Benchmarks.kotlin()
        }
    }*/
}