import org.marioarias.monkey.benchmark.Benchmarks

external val process: dynamic
fun main() {
    val args = process.argv.slice(2) as Array<String>
    if (args.isNotEmpty()) {
        when (args.first()) {
            "vm" -> Benchmarks.vm()
            "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
            "eval" -> Benchmarks.eval()
            "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
            "kotlin" -> Benchmarks.kotlin()
        }
    }
}