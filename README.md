# `monkey.kt`

Kotlin's implementation of the [Monkey Language](https://monkeylang.org/)

## Articles

https://medium.com/@mario.arias.c/comparing-kotlin-and-golang-implementations-of-the-monkey-language-3a41122ea732

https://medium.com/@mario.arias.c/comparing-kotlin-and-go-implementations-of-the-monkey-language-ii-raiders-of-the-lost-performance-b9aa09945281

https://medium.com/@mario.arias.c/comparing-kotlin-and-go-implementations-of-the-monkey-language-iii-dancing-with-segmentation-435a13c00fbd

https://marioarias.hashnode.dev/comparing-implementations-of-the-monkey-language-xi-going-native-and-js-with-kotlin

## Status

The two books ([Writing An Interpreter In Go](https://interpreterbook.com/)
and [Writing A Compiler in Go](https://compilerbook.com/)) are implemented.

```text
Hello, this is the monkey.kt programming language
Feel free to type any command
>>> let add = fn(a, b) { a + b; }
fn(a, b) {
    (a + b)
}
>>> let x = add(1, 2)
3
>>> add(x, 5)
8
```

For an implementation of the interpreter with macros (but not a compiler) check the
branch [eval-macros](https://github.com/MarioAriasC/monkey.kt/tree/eval-macros)

## Execution

There are six different executable environments, JVM, Native, JavaScript, WASM JS, WASM Wasi and GraalVM. Each executable has 2
different shell scripts, `build` and `benchmarks`. JVM, Native and GraalVM also have a proper Monkey REPL

| Executable environment | Build                | REPL             | Benchmarks                                  |
|------------------------|----------------------|------------------|---------------------------------------------|
| JVM                    | `build-jvm.sh`       | `repl-jvm.sh`    | `benchmarks-jvm.sh`                         |
| Native                 | `build-native.sh`    | `repl-native.sh` | `benchmarks-native.sh`                      |
| JavaScript             | `build-js.sh`        | NA               | `benchmarks-node.sh` or `benchmarks-bun.sh` |
| WASM JS                | `build-wasm-js.sh`   | NA               | `benchmarks-wasm-js.sh`                     |
| WASM Wasi              | `build-wasm-wasi.sh` | NA               | `benchmarks-wasm-wasi.sh`                   |
| GraalVM                | `build-graal.sh`     | `repl-graal.sh`  | `benchmarks-graal.sh`                       |

You must run the `build-[ENV].sh` before running `repl-[ENV].sh` or `benchmarks-[ENV].sh`

### JVM

For *nix systems, run the following command:

```shell
$ ./build-jvm.sh
```

And then:

```shell
$ ./repl-jvm.sh
```

### Kotlin Native

For *nix systems, run the following command:

```shell
$ ./build-native.sh
```

And then:

```shell
$ ./repl-native.sh
```

### Kotlin JS

For *nix systems, run the following command:

```shell
$ ./build-js.sh
```

The JS REPL is not working at the moment. But you can still run the benchmarks using [Node](https://nodejs.org/en/)
or [Bun](https://bun.sh/) (Must be installed beforehand)

### Kotlin WASM JS

For *nix systems, run the following command:

```shell
$ ./build-wasm-js.sh
```

### Kotlin WASM Wasi

For *nix systems, run the following command:

```shell
$ ./build-wasm-wasi.sh
```

The WASM REPL are not working at the moment. But you can still run the benchmarks, you must
have [Node](https://nodejs.org/en/) installed

### GraalVM

To build the GraalVM version, you must export the env variable `GRAALVM_HOME` pointing to your GraalVM installation

```shell
export GRAALVM_HOME="$SDKMAN_DIR/candidates/java/25.0.2-graalce"
```

Then you can run:

```shell
$ ./build-graal.sh
```

# Benchmarks

To run the standard Monkey language benchmarks (`fibonacci(35);`) add a parameter `vm`, `vm-fast`, `eval` or `eval-fast`
to the benchmark script.

Example

```shell
$ ./benchmarks-jvm.sh vm
```

```text
engine=vm, result=9227465, duration=5.797319458s
```

All the benchmarks tested on an MBP 13" M5 32GB

If you want to run proper benchmarks, I recommend [hyperfine](https://github.com/sharkdp/hyperfine)

```shell
$ hyperfine -w 3 './benchmarks-bun.sh vm-fast' './benchmarks-jvm.sh vm-fast' './benchmarks-native.sh vm-fast' './benchmarks-node.sh vm-fast' './benchmarks-wasm-wasi.sh vm-fast' './benchmarks-wasm-js.sh vm-fast' './benchmarks-graal.sh vm-fast' --export-json ../kotlin-all-vm-fast.json && hyperfine -w 3 './benchmarks-bun.sh eval-fast' './benchmarks-jvm.sh eval-fast' './benchmarks-native.sh eval-fast' './benchmarks-node.sh eval-fast' './benchmarks-wasm-wasi.sh eval-fast' './benchmarks-wasm-js.sh eval-fast' './benchmarks-graal.sh eval-fast'  --export-json ../kotlin-all-eval-fast.json
```

```text
Benchmark 1: ./benchmarks-bun.sh vm-fast
  Time (mean ± σ):     12.721 s ±  0.533 s    [User: 12.621 s, System: 0.199 s]
  Range (min … max):   12.212 s … 13.641 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      3.784 s ±  0.143 s    [User: 3.745 s, System: 0.103 s]
  Range (min … max):    3.449 s …  3.940 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):      4.325 s ±  0.092 s    [User: 4.750 s, System: 0.058 s]
  Range (min … max):    4.177 s …  4.522 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     13.773 s ±  0.353 s    [User: 13.581 s, System: 0.081 s]
  Range (min … max):   13.217 s … 14.231 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh vm-fast
  Time (mean ± σ):     11.643 s ±  0.187 s    [User: 11.512 s, System: 0.057 s]
  Range (min … max):   11.448 s … 12.024 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh vm-fast
  Time (mean ± σ):     11.651 s ±  0.160 s    [User: 11.503 s, System: 0.063 s]
  Range (min … max):   11.345 s … 11.913 s    10 runs

Benchmark 7: ./benchmarks-graal.sh vm-fast
  Time (mean ± σ):      5.608 s ±  0.207 s    [User: 5.419 s, System: 0.065 s]
  Range (min … max):    5.406 s …  6.040 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.14 ± 0.05 times faster than ./benchmarks-native.sh vm-fast
    1.48 ± 0.08 times faster than ./benchmarks-graal.sh vm-fast
    3.08 ± 0.13 times faster than ./benchmarks-wasm-wasi.sh vm-fast
    3.08 ± 0.12 times faster than ./benchmarks-wasm-js.sh vm-fast
    3.36 ± 0.19 times faster than ./benchmarks-bun.sh vm-fast
    3.64 ± 0.17 times faster than ./benchmarks-node.sh vm-fast

Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     13.654 s ±  1.100 s    [User: 13.384 s, System: 0.419 s]
  Range (min … max):   12.378 s … 16.173 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      2.827 s ±  0.105 s    [User: 2.805 s, System: 0.088 s]
  Range (min … max):    2.708 s …  3.049 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):      9.900 s ±  0.138 s    [User: 10.493 s, System: 0.110 s]
  Range (min … max):    9.680 s … 10.170 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     14.869 s ±  0.340 s    [User: 14.689 s, System: 0.104 s]
  Range (min … max):   14.484 s … 15.517 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh eval-fast
  Time (mean ± σ):      6.641 s ±  0.114 s    [User: 6.563 s, System: 0.052 s]
  Range (min … max):    6.513 s …  6.817 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh eval-fast
  Time (mean ± σ):      6.394 s ±  0.104 s    [User: 6.325 s, System: 0.048 s]
  Range (min … max):    6.212 s …  6.550 s    10 runs

Benchmark 7: ./benchmarks-graal.sh eval-fast
  Time (mean ± σ):      5.531 s ±  0.158 s    [User: 5.366 s, System: 0.086 s]
  Range (min … max):    5.255 s …  5.765 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    1.96 ± 0.09 times faster than ./benchmarks-graal.sh eval-fast
    2.26 ± 0.09 times faster than ./benchmarks-wasm-js.sh eval-fast
    2.35 ± 0.10 times faster than ./benchmarks-wasm-wasi.sh eval-fast
    3.50 ± 0.14 times faster than ./benchmarks-native.sh eval-fast
    4.83 ± 0.43 times faster than ./benchmarks-bun.sh eval-fast
    5.26 ± 0.23 times faster than ./benchmarks-node.sh eval-fast  
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9)

```text
ruby plot.rb kotlin-all-vm-fast.json
                                     ┌                                                                                                    ┐
         ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 12.720726591619998
         ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■ 3.784409079320001
      ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■ 4.32549654192
        ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.77279036692
   ./benchmarks-wasm-wasi.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.64316867502
     ./benchmarks-wasm-js.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.65118608352
       ./benchmarks-graal.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 5.608133970820001
                                     └                                                                                                    ┘

ruby plot.rb kotlin-all-eval-fast.json
                                       ┌                                                                                                    ┐
         ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.653818654039998
         ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■■■■■■■■ 2.82661181244
      ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 9.900208979139999
        ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 14.86887256674
   ./benchmarks-wasm-wasi.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.64057439994
     ./benchmarks-wasm-js.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.39415533304
       ./benchmarks-graal.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 5.531197912640001
                                       └                                                                                                    ┘
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```
