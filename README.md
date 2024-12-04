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
$ hyperfine -w 3 './benchmarks-bun.sh vm-fast' './benchmarks-jvm.sh vm-fast' './benchmarks-native.sh vm-fast' './benchmarks-node.sh vm-fast' --export-json ../vm-fast-inline-last.json && hyperfine -w 3 './benchmarks-bun.sh eval-fast' './benchmarks-jvm.sh eval-fast' './benchmarks-native.sh eval-fast' './benchmarks-node.sh eval-fast' --export-json ../eval-fast-inline-last.json
```
```text
Benchmark 1: ./benchmarks-bun.sh vm-fast
  Time (mean ± σ):     22.028 s ±  0.699 s    [User: 22.203 s, System: 0.252 s]
  Range (min … max):   20.903 s … 22.970 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      4.958 s ±  0.021 s    [User: 4.953 s, System: 0.176 s]
  Range (min … max):    4.929 s …  5.002 s    10 runs

Benchmark 3: ./benchmarks-native.sh vm-fast
  Time (mean ± σ):      9.881 s ±  0.190 s    [User: 10.193 s, System: 0.064 s]
  Range (min … max):    9.729 s … 10.401 s    10 runs

Benchmark 4: ./benchmarks-node.sh vm-fast
  Time (mean ± σ):     21.782 s ±  0.178 s    [User: 21.806 s, System: 0.083 s]
  Range (min … max):   21.447 s … 22.107 s    10 runs

Summary
  ./benchmarks-jvm.sh vm-fast ran
    1.99 ± 0.04 times faster than ./benchmarks-native.sh vm-fast
    4.39 ± 0.04 times faster than ./benchmarks-node.sh vm-fast
    4.44 ± 0.14 times faster than ./benchmarks-bun.sh vm-fast

Benchmark 1: ./benchmarks-bun.sh eval-fast
  Time (mean ± σ):     32.877 s ±  0.290 s    [User: 32.963 s, System: 0.628 s]
  Range (min … max):   32.442 s … 33.363 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):      3.396 s ±  0.075 s    [User: 3.407 s, System: 0.187 s]
  Range (min … max):    3.317 s …  3.545 s    10 runs

Benchmark 3: ./benchmarks-native.sh eval-fast
  Time (mean ± σ):     16.539 s ±  0.162 s    [User: 16.381 s, System: 0.110 s]
  Range (min … max):   16.352 s … 16.883 s    10 runs

Benchmark 4: ./benchmarks-node.sh eval-fast
  Time (mean ± σ):     32.029 s ±  0.474 s    [User: 32.071 s, System: 0.069 s]
  Range (min … max):   31.235 s … 32.855 s    10 runs

Summary
  ./benchmarks-jvm.sh eval-fast ran
    4.87 ± 0.12 times faster than ./benchmarks-native.sh eval-fast
    9.43 ± 0.25 times faster than ./benchmarks-node.sh eval-fast
    9.68 ± 0.23 times faster than ./benchmarks-bun.sh eval-fast
```

You can plot the results with this [script](https://gist.github.com/MarioAriasC/599204342860a161d4fe12b12f0d3de9) 

```text
❯ ruby --yjit plot.rb vm-fast-inline-last.json
                                  ┌                                                                                                    ┐
      ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 22.02788957328
      ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■ 4.957949344579999
   ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 9.88136447028
     ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 21.782049139380003
                                  └                                                                                                    ┘                                                                                                 ┘

❯ ruby --yjit plot.rb eval-fast-inline-last.json
                                    ┌                                                                                                    ┐
      ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 32.87707063506
      ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■ 3.39647823516
   ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 16.539440131659997
     ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 32.02919512696
                                    └                                                                                                    ┘
```


## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```