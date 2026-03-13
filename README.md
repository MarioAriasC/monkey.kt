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
  Time (mean ± σ):     11.268 s ±  0.069 s    [User: 11.353 s, System: 0.109 s]
  Range (min … max):   11.196 s … 11.393 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      3.570 s ±  0.041 s    [User: 3.573 s, System: 0.075 s]
  Range (min … max):    3.497 s …  3.612 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):      7.904 s ±  0.049 s    [User: 8.124 s, System: 0.063 s]
  Range (min … max):    7.843 s …  8.000 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     12.690 s ±  0.280 s    [User: 12.627 s, System: 0.051 s]
  Range (min … max):   12.412 s … 13.089 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh vm-fast
  Time (mean ± σ):     11.006 s ±  0.376 s    [User: 10.940 s, System: 0.043 s]
  Range (min … max):   10.537 s … 11.549 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh vm-fast
  Time (mean ± σ):     11.664 s ±  0.219 s    [User: 11.615 s, System: 0.037 s]
  Range (min … max):   11.186 s … 11.865 s    10 runs

Benchmark 7: ./benchmarks-graal.sh vm-fast
  Time (mean ± σ):      5.077 s ±  0.154 s    [User: 5.036 s, System: 0.034 s]
  Range (min … max):    4.861 s …  5.331 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.42 ± 0.05 times faster than ./benchmarks-graal.sh vm-fast
    2.21 ± 0.03 times faster than ./benchmarks-native.sh vm-fast
    3.08 ± 0.11 times faster than ./benchmarks-wasm-wasi.sh vm-fast
    3.16 ± 0.04 times faster than ./benchmarks-bun.sh vm-fast
    3.27 ± 0.07 times faster than ./benchmarks-wasm-js.sh vm-fast
    3.56 ± 0.09 times faster than ./benchmarks-node.sh vm-fast

Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     11.260 s ±  0.353 s    [User: 11.345 s, System: 0.240 s]
  Range (min … max):   10.744 s … 11.699 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      2.410 s ±  0.094 s    [User: 2.418 s, System: 0.073 s]
  Range (min … max):    2.216 s …  2.541 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     18.105 s ±  1.900 s    [User: 19.273 s, System: 0.122 s]
  Range (min … max):   16.524 s … 22.622 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     13.757 s ±  0.267 s    [User: 13.704 s, System: 0.071 s]
  Range (min … max):   13.383 s … 14.160 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh eval-fast
  Time (mean ± σ):      6.952 s ±  0.107 s    [User: 6.917 s, System: 0.042 s]
  Range (min … max):    6.701 s …  7.050 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh eval-fast
  Time (mean ± σ):      6.542 s ±  0.166 s    [User: 6.494 s, System: 0.047 s]
  Range (min … max):    6.402 s …  6.977 s    10 runs

Benchmark 7: ./benchmarks-graal.sh eval-fast
  Time (mean ± σ):      4.780 s ±  0.088 s    [User: 4.696 s, System: 0.072 s]
  Range (min … max):    4.678 s …  4.949 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    1.98 ± 0.09 times faster than ./benchmarks-graal.sh eval-fast
    2.72 ± 0.13 times faster than ./benchmarks-wasm-js.sh eval-fast
    2.89 ± 0.12 times faster than ./benchmarks-wasm-wasi.sh eval-fast
    4.67 ± 0.23 times faster than ./benchmarks-bun.sh eval-fast
    5.71 ± 0.25 times faster than ./benchmarks-node.sh eval-fast
    7.51 ± 0.84 times faster than ./benchmarks-native.sh eval-fast
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9)

```text
❯ ruby --yjit plot.rb kotlin-all-vm-fast.json
                                     ┌                                                                                                    ┐
         ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.268465726
         ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■ 3.5697412133
      ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 7.9041775258000015
        ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 12.690447451
   ./benchmarks-wasm-wasi.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.0056405051
     ./benchmarks-wasm-js.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.664382513300001
       ./benchmarks-graal.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 5.0774031677
                                     └                                                                                                    ┘

❯ ruby --yjit plot.rb kotlin-all-eval-fast.json
                                       ┌                                                                                                    ┐
         ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.259901410020001
         ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■■■ 2.40950986012
      ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 18.105055101819996
        ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.757043997519997
   ./benchmarks-wasm-wasi.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.95244012262
     ./benchmarks-wasm-js.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.541953114020001
       ./benchmarks-graal.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■ 4.7802565682200004
                                       └                                                                                                    ┘
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```