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
  Time (mean ± σ):     11.434 s ±  0.146 s    [User: 11.512 s, System: 0.111 s]
  Range (min … max):   11.274 s … 11.692 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      3.658 s ±  0.049 s    [User: 3.662 s, System: 0.073 s]
  Range (min … max):    3.581 s …  3.758 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):      8.368 s ±  0.191 s    [User: 8.559 s, System: 0.069 s]
  Range (min … max):    8.057 s …  8.591 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     13.572 s ±  0.355 s    [User: 13.469 s, System: 0.054 s]
  Range (min … max):   13.055 s … 14.036 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh vm-fast
  Time (mean ± σ):     14.001 s ±  0.299 s    [User: 13.932 s, System: 0.048 s]
  Range (min … max):   13.631 s … 14.620 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh vm-fast
  Time (mean ± σ):     11.506 s ±  0.185 s    [User: 11.452 s, System: 0.041 s]
  Range (min … max):   11.238 s … 11.737 s    10 runs

Benchmark 7: ./benchmarks-graal.sh vm-fast
  Time (mean ± σ):      5.099 s ±  0.075 s    [User: 5.042 s, System: 0.039 s]
  Range (min … max):    5.025 s …  5.271 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.39 ± 0.03 times faster than ./benchmarks-graal.sh vm-fast
    2.29 ± 0.06 times faster than ./benchmarks-native.sh vm-fast
    3.13 ± 0.06 times faster than ./benchmarks-bun.sh vm-fast
    3.15 ± 0.07 times faster than ./benchmarks-wasm-js.sh vm-fast
    3.71 ± 0.11 times faster than ./benchmarks-node.sh vm-fast
    3.83 ± 0.10 times faster than ./benchmarks-wasm-wasi.sh vm-fast
        
Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     12.516 s ±  0.119 s    [User: 12.561 s, System: 0.262 s]
  Range (min … max):   12.362 s … 12.774 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      2.482 s ±  0.099 s    [User: 2.489 s, System: 0.073 s]
  Range (min … max):    2.372 s …  2.719 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     17.715 s ±  0.884 s    [User: 18.769 s, System: 0.139 s]
  Range (min … max):   16.740 s … 19.737 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     15.379 s ±  0.437 s    [User: 15.164 s, System: 0.091 s]
  Range (min … max):   14.644 s … 15.923 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh eval-fast
  Time (mean ± σ):      7.749 s ±  0.162 s    [User: 7.662 s, System: 0.043 s]
  Range (min … max):    7.548 s …  7.991 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh eval-fast
  Time (mean ± σ):      6.948 s ±  0.266 s    [User: 6.860 s, System: 0.051 s]
  Range (min … max):    6.659 s …  7.498 s    10 runs

Benchmark 7: ./benchmarks-graal.sh eval-fast
  Time (mean ± σ):      4.957 s ±  0.099 s    [User: 4.844 s, System: 0.072 s]
  Range (min … max):    4.843 s …  5.121 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    2.00 ± 0.09 times faster than ./benchmarks-graal.sh eval-fast
    2.80 ± 0.15 times faster than ./benchmarks-wasm-js.sh eval-fast
    3.12 ± 0.14 times faster than ./benchmarks-wasm-wasi.sh eval-fast
    5.04 ± 0.21 times faster than ./benchmarks-bun.sh eval-fast
    6.20 ± 0.30 times faster than ./benchmarks-node.sh eval-fast
    7.14 ± 0.46 times faster than ./benchmarks-native.sh eval-fast
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9)

```text
❯ ruby --yjit plot.rb kotlin-all-vm-fast.json
                                     ┌                                                                                                    ┐
         ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.433623209219999
         ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■ 3.6581846965200002
      ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 8.368384117620002
        ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.57247149652
   ./benchmarks-wasm-wasi.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 14.00070348412
     ./benchmarks-wasm-js.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.50574438412
       ./benchmarks-graal.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 5.09925613812
                                     └                                                                                                    ┘

❯ ruby --yjit plot.rb kotlin-all-eval-fast.json
                                       ┌                                                                                                    ┐
         ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 12.5159081373
         ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■■■■ 2.4815598624
      ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 17.7152589751
        ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 15.378502987500003
   ./benchmarks-wasm-wasi.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 7.748892208400001
     ./benchmarks-wasm-js.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.947508570999998
       ./benchmarks-graal.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■ 4.9566947667
                                       └                                                                                                    ┘
                                       └                                                                                                    ┘
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```