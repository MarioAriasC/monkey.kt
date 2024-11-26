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

| Executable environment | Build             | REPL             | Benchmarks                                  |
|------------------------|-------------------|------------------|---------------------------------------------|
| JVM                    | `build-jvm.sh`    | `repl-jvm.sh`    | `benchmarks-jvm.sh`                         |
| Native                 | `build-native.sh` | `repl-native.sh` | `benchmarks-native.sh`                      |
| JavaScript             | `build-js.sh`     | NA               | `benchmarks-node.sh` or `benchmarks-bun.sh` |

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
$ hyperfine -w 3 './benchmarks-bun.sh vm-fast' './benchmarks-jvm.sh vm-fast' './benchmarks-native.sh vm-fast' './benchmarks-node.sh vm-fast' --export-json ../vm-fast.json
```
```text
Benchmark 1: ./benchmarks-bun.sh vm-fast
  Time (mean ± σ):     23.204 s ±  0.433 s    [User: 23.395 s, System: 0.291 s]
  Range (min … max):   22.584 s … 23.896 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      4.951 s ±  0.027 s    [User: 4.930 s, System: 0.195 s]
  Range (min … max):    4.910 s …  5.002 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):     12.112 s ±  0.200 s    [User: 12.588 s, System: 0.056 s]
  Range (min … max):   11.917 s … 12.538 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     22.323 s ±  0.389 s    [User: 22.358 s, System: 0.080 s]
  Range (min … max):   21.730 s … 23.067 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    2.45 ± 0.04 times faster than ./benchmarks-native.sh vm-fast
    4.51 ± 0.08 times faster than ./benchmarks-node.sh vm-fast
    4.69 ± 0.09 times faster than ./benchmarks-bun.sh vm-fast
```
```shell
$ hyperfine -w 3 './benchmarks-bun.sh eval-fast' './benchmarks-jvm.sh eval-fast' './benchmarks-native.sh eval-fast' './benchmarks-node.sh eval-fast' --export-json ../eval-fast.json
```
```text
Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     34.869 s ±  0.285 s    [User: 35.092 s, System: 0.817 s]
  Range (min … max):   34.315 s … 35.248 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      3.929 s ±  0.289 s    [User: 4.201 s, System: 0.257 s]
  Range (min … max):    3.527 s …  4.365 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     15.132 s ±  0.099 s    [User: 14.905 s, System: 0.107 s]
  Range (min … max):   15.011 s … 15.264 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     31.162 s ±  0.544 s    [User: 31.209 s, System: 0.076 s]
  Range (min … max):   30.459 s … 31.981 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    3.85 ± 0.28 times faster than ./benchmarks-native.sh eval-fast
    7.93 ± 0.60 times faster than ./benchmarks-node.sh eval-fast
    8.87 ± 0.66 times faster than ./benchmarks-bun.sh eval-fast
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9) 

```text
❯ ruby --yjit plot.rb vm-fast.json
                                  ┌                                                                                                    ┐
      ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 23.2038023168
      ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■ 4.9508734608
   ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 12.111709727000001
     ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 22.323175457900003
                                  └                                                                                                    ┘

❯ ruby --yjit plot.rb eval-fast.json
                                    ┌                                                                                                    ┐
      ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 34.86919739186
      ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■ 3.92911335406
   ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 15.13231887616
     ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 31.16230034386
                                    └                                                                                                    ┘
```


## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```