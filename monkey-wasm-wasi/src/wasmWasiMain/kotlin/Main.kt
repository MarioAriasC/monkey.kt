import org.marioarias.monkey.benchmark.Benchmarks
import org.marioarias.monkey.wasm.argv


fun main() {
    when (argv().last()) {
        "vm" -> Benchmarks.vm()
        "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
        "eval" -> Benchmarks.eval()
        "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
        "kotlin" -> Benchmarks.kotlin()
    }
}
