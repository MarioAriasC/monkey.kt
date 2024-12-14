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

There are three different executable environments, JVM, Native and JavaScript Each executable has 3 different
shell scripts, `build`, `repl`, `benchmarks`

| Executable environment | Build                | REPL             | Benchmarks                                  |
|------------------------|----------------------|------------------|---------------------------------------------|
| JVM                    | `build-jvm.sh`       | `repl-jvm.sh`    | `benchmarks-jvm.sh`                         |
| Native                 | `build-native.sh`    | `repl-native.sh` | `benchmarks-native.sh`                      |
| JavaScript             | `build-js.sh`        | NA               | `benchmarks-node.sh` or `benchmarks-bun.sh` |
| WASM JS                | `build-wasm-js.sh`   | NA               | `benchmarks-wasm-js.sh`                     |
| WASM Wasi              | `build-wasm-wasi.sh` | NA               | `benchmarks-wasm-wasi.sh`                   |


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

The JS REPL is not working at the moment. But you can still run the benchmarks using [Node](https://nodejs.org/en/) or [Bun](https://bun.sh/) (Must be installed beforehand)
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

The WASM REPL are not working at the moment. But you can still run the benchmarks, you must have [Node](https://nodejs.org/en/) installed


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

 
All the benchmarks tested on a Pop!_OS Laptop AMD Ryzen 9 5900HX
 
If you want to run proper benchmarks, I recommend [hyperfine](https://github.com/sharkdp/hyperfine)

```shell
$ hyperfine -w 3 './benchmarks-bun.sh vm-fast' './benchmarks-jvm.sh vm-fast' './benchmarks-native.sh vm-fast' './benchmarks-node.sh vm-fast' './benchmarks-wasm-wasi.sh vm-fast' './benchmarks-wasm-js.sh vm-fast' --export-json ../kotlin-wasm-vm-fast.json && hyperfine -w 3 './benchmarks-bun.sh eval-fast' './benchmarks-jvm.sh eval-fast' './benchmarks-native.sh eval-fast' './benchmarks-node.sh eval-fast' './benchmarks-wasm-wasi.sh eval-fast' './benchmarks-wasm-js.sh eval-fast'  --export-json ../kotlin-wasm-eval-fast.json
```
```text
Benchmark 1: ./benchmarks-bun.sh vm-fast
  Time (mean ± σ):     22.705 s ±  0.570 s    [User: 22.841 s, System: 0.706 s]
  Range (min … max):   21.658 s … 23.473 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      5.124 s ±  0.031 s    [User: 5.084 s, System: 0.309 s]
  Range (min … max):    5.096 s …  5.204 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):     10.129 s ±  0.171 s    [User: 10.400 s, System: 0.169 s]
  Range (min … max):    9.917 s … 10.459 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     22.852 s ±  0.412 s    [User: 22.524 s, System: 0.435 s]
  Range (min … max):   22.196 s … 23.565 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh vm-fast
  Time (mean ± σ):     15.802 s ±  0.717 s    [User: 15.561 s, System: 0.261 s]
  Range (min … max):   15.277 s … 17.385 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh vm-fast
  Time (mean ± σ):     17.045 s ±  0.387 s    [User: 16.813 s, System: 0.246 s]
  Range (min … max):   16.664 s … 17.829 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.98 ± 0.04 times faster than ./benchmarks-native.sh vm-fast
    3.08 ± 0.14 times faster than ./benchmarks-wasm-wasi.sh vm-fast
    3.33 ± 0.08 times faster than ./benchmarks-wasm-js.sh vm-fast
    4.43 ± 0.11 times faster than ./benchmarks-bun.sh vm-fast
    4.46 ± 0.08 times faster than ./benchmarks-node.sh vm-fast
Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     34.566 s ±  0.502 s    [User: 34.681 s, System: 1.932 s]
  Range (min … max):   33.938 s … 35.627 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      3.621 s ±  0.096 s    [User: 3.614 s, System: 0.383 s]
  Range (min … max):    3.526 s …  3.794 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     16.977 s ±  0.239 s    [User: 16.577 s, System: 0.374 s]
  Range (min … max):   16.556 s … 17.421 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     32.926 s ±  1.088 s    [User: 32.679 s, System: 0.361 s]
  Range (min … max):   31.335 s … 35.320 s    10 runs

Benchmark 5: ./benchmarks-wasm-wasi.sh eval-fast
  Time (mean ± σ):     11.295 s ±  0.251 s    [User: 11.077 s, System: 0.276 s]
  Range (min … max):   10.929 s … 11.769 s    10 runs

Benchmark 6: ./benchmarks-wasm-js.sh eval-fast
  Time (mean ± σ):     11.310 s ±  0.189 s    [User: 11.090 s, System: 0.278 s]
  Range (min … max):   11.034 s … 11.655 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    3.12 ± 0.11 times faster than ./benchmarks-wasm-wasi.sh eval-fast
    3.12 ± 0.10 times faster than ./benchmarks-wasm-js.sh eval-fast
    4.69 ± 0.14 times faster than ./benchmarks-native.sh eval-fast
    9.09 ± 0.39 times faster than ./benchmarks-node.sh eval-fast
    9.55 ± 0.29 times faster than ./benchmarks-bun.sh eval-fast
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9) 

```text
❯ ruby --yjit plot.rb kotlin-wasm-vm-fast.json
                                     ┌                                                                                                    ┐
         ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 22.704543833360002
         ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■ 5.12403986566
      ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 10.128824652159999
        ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 22.852397735060002
   ./benchmarks-wasm-wasi.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 15.80210154176
     ./benchmarks-wasm-js.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 17.04475952096
                                     └                                                                                                    ┘

❯ ruby --yjit plot.rb kotlin-wasm-eval-fast.json
                                       ┌                                                                                                    ┐
         ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 34.566014226200004
         ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■ 3.6206429516
      ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 16.9771580748
        ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 32.925853603600004
   ./benchmarks-wasm-wasi.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.295171940900001
     ./benchmarks-wasm-js.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■ 11.3097880403
                                       └                                                                                                    ┘
```


## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```