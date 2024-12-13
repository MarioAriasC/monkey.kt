import org.marioarias.monkey.benchmark.Benchmarks
import org.marioarias.monkey.wasm.argv


fun main() {
    val args = argv()
    when (args.first()) {
        "vm" -> Benchmarks.vm()
        "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
        "eval" -> Benchmarks.eval()
        "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
    }
}