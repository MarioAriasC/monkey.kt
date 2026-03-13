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
  Time (mean ± σ):     11.451 s ±  0.211 s    [User: 11.511 s, System: 0.120 s]
  Range (min … max):   11.268 s … 11.939 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      3.635 s ±  0.042 s    [User: 3.626 s, System: 0.083 s]
  Range (min … max):    3.577 s …  3.730 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):      7.048 s ±  0.373 s    [User: 7.276 s, System: 0.063 s]
  Range (min … max):    6.524 s …  7.785 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     13.293 s ±  0.475 s    [User: 13.165 s, System: 0.071 s]
  Range (min … max):   12.802 s … 14.107 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh vm-fast
  Time (mean ± σ):     11.157 s ±  0.192 s    [User: 11.052 s, System: 0.059 s]
  Range (min … max):   10.968 s … 11.557 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh vm-fast
  Time (mean ± σ):     11.458 s ±  0.318 s    [User: 11.345 s, System: 0.064 s]
  Range (min … max):   11.107 s … 12.213 s    10 runs

Benchmark 7: ./benchmarks-graal.sh vm-fast
  Time (mean ± σ):      5.100 s ±  0.099 s    [User: 5.022 s, System: 0.046 s]
  Range (min … max):    5.018 s …  5.358 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.40 ± 0.03 times faster than ./benchmarks-graal.sh vm-fast
    1.94 ± 0.11 times faster than ./benchmarks-native.sh vm-fast
    3.07 ± 0.06 times faster than ./benchmarks-wasm-wasi.sh vm-fast
    3.15 ± 0.07 times faster than ./benchmarks-bun.sh vm-fast
    3.15 ± 0.09 times faster than ./benchmarks-wasm-js.sh vm-fast
    3.66 ± 0.14 times faster than ./benchmarks-node.sh vm-fast

Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     11.933 s ±  0.328 s    [User: 11.959 s, System: 0.290 s]
  Range (min … max):   11.580 s … 12.700 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      2.632 s ±  0.215 s    [User: 2.604 s, System: 0.088 s]
  Range (min … max):    2.355 s …  3.048 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     16.477 s ±  0.671 s    [User: 17.210 s, System: 0.141 s]
  Range (min … max):   15.843 s … 17.858 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     13.818 s ±  0.285 s    [User: 13.714 s, System: 0.092 s]
  Range (min … max):   13.520 s … 14.405 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh eval-fast
  Time (mean ± σ):      6.604 s ±  0.107 s    [User: 6.540 s, System: 0.053 s]
  Range (min … max):    6.478 s …  6.789 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh eval-fast
  Time (mean ± σ):      6.633 s ±  0.287 s    [User: 6.534 s, System: 0.061 s]
  Range (min … max):    6.396 s …  7.345 s    10 runs

Benchmark 7: ./benchmarks-graal.sh eval-fast
  Time (mean ± σ):      4.941 s ±  0.197 s    [User: 4.789 s, System: 0.089 s]
  Range (min … max):    4.760 s …  5.266 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    1.88 ± 0.17 times faster than ./benchmarks-graal.sh eval-fast
    2.51 ± 0.21 times faster than ./benchmarks-wasm-wasi.sh eval-fast
    2.52 ± 0.23 times faster than ./benchmarks-wasm-js.sh eval-fast
    4.53 ± 0.39 times faster than ./benchmarks-bun.sh eval-fast
    5.25 ± 0.44 times faster than ./benchmarks-node.sh eval-fast
    6.26 ± 0.57 times faster than ./benchmarks-native.sh eval-fast```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9)

```text
❯ ruby --yjit plot.rb kotlin-all-vm-fast.json
                                     ┌                                                                                                    ┐
         ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.45143038254
         ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■ 3.63499144484
      ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 7.048361003240001
        ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.292569690840002
   ./benchmarks-wasm-wasi.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.156858049240002
     ./benchmarks-wasm-js.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.45819161164
       ./benchmarks-graal.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 5.10002622004
                                     └                                                                                                    ┘

ruby --yjit plot.rb kotlin-all-eval-fast.json
                                       ┌                                                                                                    ┐
         ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.93287168418
         ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■■■■■ 2.63199487578
      ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 16.477025800979998
        ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 13.81827043408
   ./benchmarks-wasm-wasi.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.60442814258
     ./benchmarks-wasm-js.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 6.63338047598
       ./benchmarks-graal.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■ 4.94059325088
                                       └                                                                                                    ┘
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```