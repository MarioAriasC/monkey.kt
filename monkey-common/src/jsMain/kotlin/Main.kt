import org.marioarias.monkey.benchmark.Benchmarks

external val process: dynamic
fun main() {
    val args = process.argv.slice(2) as Array<String>
    if (args.isNotEmpty()) {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.fastInput)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.fastInput)
            "kotlin" -> Benchmarks.kotlin()
        }
    }
}