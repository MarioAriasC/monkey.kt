import org.marioarias.monkey.benchmark.Benchmarks
import kotlin.js.JsString

@JsModule("node:process")
private external object Process {
    val argv: JsArray<JsString>
}

fun main() {
    when (Process.argv.toArray().last().toString()) {
        "vm" -> Benchmarks.vm()
        "vm-fast" -> Benchmarks.vm(Benchmarks.FAST_INPUT)
        "eval" -> Benchmarks.eval()
        "eval-fast" -> Benchmarks.eval(Benchmarks.FAST_INPUT)
        "kotlin" -> Benchmarks.kotlin()
    }
}