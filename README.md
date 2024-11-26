# `monkey.kt`

Kotlin's implementation of the [Monkey Language](https://monkeylang.org/)

## Articles

https://medium.com/@mario.arias.c/comparing-kotlin-and-golang-implementations-of-the-monkey-language-3a41122ea732

https://medium.com/@mario.arias.c/comparing-kotlin-and-go-implementations-of-the-monkey-language-ii-raiders-of-the-lost-performance-b9aa09945281

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

There are four different executable environments, JVM, GraalVM Native Image, Native and JavaScript Each executable has 3 different
shell scripts, `build`, `repl`, `benchmarks`

| Executable environment | Build             | REPL             | Benchmarks                                  |
|------------------------|-------------------|------------------|---------------------------------------------|
| JVM                    | `build-jvm.sh`    | `repl-jvm.sh`    | `benchmarks-jvm.sh`                         |
| Graal                  | `build-graal.sh`  | `repl-graal.sh`  | `benchmarks-graal.sh`                       |
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

### GraalVM Native Image

To run the application with [GraalVM](https://www.graalvm.org/) Native Image, you need to follow certain steps:

- Install GraalVM, I recommend using [SDKMAN](https://sdkman.io/) (Not just for GraalVM but for any JVM tool in general)
- Install the `native-image` [executable](https://www.graalvm.org/reference-manual/native-image/#install-native-image)
- Create a GRAALVM_HOME environment variable. On *nix
  systems `export GRAALVM_HOME="$HOME/.sdkman/candidates/java/21.2.0.r11-grl/` or your equivalent GraalVM location
- Run the command

```shell
$ ./build-graal.sh
```

And then:

```shell
$ ./repl-graal.sh
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

The JS REPL is not working at the moment. But you can still run the benchmarks using [Node](https://nodejs.org/en/) or [Bun](https://bun.sh/) (Must be installed before hand)

# Benchmarks

To run the standard Monkey language benchmarks (`fibonacci(35);`) add a parameter `vm`, `vm-fast`, `eval` or `eval-fast`
to the benchmark script.

Example

```shell
$ ./benchmarks-jvm.sh vm
engine=vm, result=9227465, duration=7.516433414s
```

```shell
$ ./benchmarks-graal.sh eval  
engine=eval, result=9227465, duration=22.173455585s
```
 
All the benchmarks tested on a MBP 15-inch 2019. Intel Core i9 2.3Ghz 8-Core, 32 GB 2400 MHZ DDR4
 
If you want to run proper benchmarks, I recommend [hyperfine](https://github.com/sharkdp/hyperfine)

```shell
$ hyperfine --warmup 3 './benchmarks-jvm.sh vm' './benchmarks-jvm.sh vm-fast' './benchmarks-jvm.sh eval' './benchmarks-jvm.sh eval-fast'
Benchmark 1: ./benchmarks-jvm.sh vm
  Time (mean ± σ):      9.505 s ±  0.788 s    [User: 9.759 s, System: 0.262 s]
  Range (min … max):    8.639 s … 10.745 s    10 runs

Benchmark 2: ./benchmarks-jvm.sh vm-fast
  Time (mean ± σ):      6.633 s ±  0.196 s    [User: 6.837 s, System: 0.206 s]
  Range (min … max):    6.317 s …  6.908 s    10 runs

Benchmark 3: ./benchmarks-jvm.sh eval
  Time (mean ± σ):     13.368 s ±  0.943 s    [User: 13.643 s, System: 0.261 s]
  Range (min … max):   11.970 s … 14.961 s    10 runs

Benchmark 4: ./benchmarks-jvm.sh eval-fast
  Time (mean ± σ):     10.321 s ±  0.760 s    [User: 10.713 s, System: 0.256 s]
  Range (min … max):    9.551 s … 11.455 s    10 runs

Summary
  './benchmarks-jvm.sh vm-fast' ran
    1.43 ± 0.13 times faster than './benchmarks-jvm.sh vm'
    1.56 ± 0.12 times faster than './benchmarks-jvm.sh eval-fast'
    2.02 ± 0.15 times faster than './benchmarks-jvm.sh eval'
```
```shell
$ hyperfine --warmup 3 './benchmarks-graal.sh vm' './benchmarks-graal.sh vm-fast' './benchmarks-graal.sh eval' './benchmarks-graal.sh eval-fast'
Benchmark 1: ./benchmarks-graal.sh vm
  Time (mean ± σ):     21.364 s ±  0.627 s    [User: 21.057 s, System: 0.165 s]
  Range (min … max):   20.759 s … 22.628 s    10 runs

Benchmark 2: ./benchmarks-graal.sh vm-fast
  Time (mean ± σ):     16.955 s ±  0.534 s    [User: 16.740 s, System: 0.136 s]
  Range (min … max):   16.158 s … 18.018 s    10 runs

Benchmark 3: ./benchmarks-graal.sh eval
  Time (mean ± σ):     20.561 s ±  0.270 s    [User: 20.321 s, System: 0.150 s]
  Range (min … max):   20.119 s … 20.910 s    10 runs

Benchmark 4: ./benchmarks-graal.sh eval-fast
  Time (mean ± σ):     16.934 s ±  0.258 s    [User: 16.730 s, System: 0.135 s]
  Range (min … max):   16.327 s … 17.206 s    10 runs

Summary
  './benchmarks-graal.sh eval-fast' ran
    1.00 ± 0.04 times faster than './benchmarks-graal.sh vm-fast'
    1.21 ± 0.02 times faster than './benchmarks-graal.sh eval'
    1.26 ± 0.04 times faster than './benchmarks-graal.sh vm'
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```

## New benchmarks
```

❯ hyperfine -w 3 './benchmarks-bun.sh kotlin' './benchmarks-jvm.sh kotlin' './benchmarks-native.sh kotlin' './benchmarks-node.sh kotlin'
Benchmark 1: ./benchmarks-bun.sh kotlin
  Time (mean ± σ):      1.769 s ±  0.109 s    [User: 1.781 s, System: 0.090 s]
  Range (min … max):    1.722 s …  2.080 s    10 runs

  Warning: Statistical outliers were detected. Consider re-running this benchmark on a quiet system without any interferences from other programs. It might help to use the '--warmup' or '--prepare' options.

Benchmark 2: ./benchmarks-jvm.sh kotlin
  Time (mean ± σ):     101.8 ms ±   1.0 ms    [User: 104.0 ms, System: 31.4 ms]
  Range (min … max):   100.2 ms … 103.7 ms    29 runs

Benchmark 3: ./benchmarks-native.sh kotlin
  Time (mean ± σ):      29.7 ms ±   0.4 ms    [User: 26.1 ms, System: 3.6 ms]
  Range (min … max):    29.0 ms …  31.2 ms    99 runs

Benchmark 4: ./benchmarks-node.sh kotlin
  Time (mean ± σ):      1.823 s ±  0.098 s    [User: 1.822 s, System: 0.027 s]
  Range (min … max):    1.558 s …  1.879 s    10 runs

  Warning: Statistical outliers were detected. Consider re-running this benchmark on a quiet system without any interferences from other programs. It might help to use the '--warmup' or '--prepare' options.

Summary
  ./benchmarks-native.sh kotlin ran
    3.43 ± 0.06 times faster than ./benchmarks-jvm.sh kotlin
   59.56 ± 3.78 times faster than ./benchmarks-bun.sh kotlin
   61.38 ± 3.43 times faster than ./benchmarks-node.sh kotlin

monkey.kt on  new-gradle via 🅶 v8.7 via ☕ v21.0.5 via 🅺 took 53s
❯ hyperfine -w 3 './benchmarks-bun.sh kotlin' './benchmarks-jvm.sh kotlin' './benchmarks-native.sh kotlin' './benchmarks-node.sh kotlin' --export-json ../kotlin.json
Benchmark 1: ./benchmarks-bun.sh kotlin
  Time (mean ± σ):      1.775 s ±  0.125 s    [User: 1.772 s, System: 0.089 s]
  Range (min … max):    1.712 s …  2.129 s    10 runs

  Warning: Statistical outliers were detected. Consider re-running this benchmark on a quiet system without any interferences from other programs. It might help to use the '--warmup' or '--prepare' options.

Benchmark 2: ./benchmarks-jvm.sh kotlin
  Time (mean ± σ):     102.4 ms ±   1.3 ms    [User: 105.4 ms, System: 30.7 ms]
  Range (min … max):   100.4 ms … 105.1 ms    29 runs

Benchmark 3: ./benchmarks-native.sh kotlin
  Time (mean ± σ):      29.9 ms ±   0.5 ms    [User: 26.2 ms, System: 3.7 ms]
  Range (min … max):    29.0 ms …  32.1 ms    98 runs

Benchmark 4: ./benchmarks-node.sh kotlin
  Time (mean ± σ):      1.871 s ±  0.207 s    [User: 1.874 s, System: 0.028 s]
  Range (min … max):    1.542 s …  2.382 s    10 runs

  Warning: Statistical outliers were detected. Consider re-running this benchmark on a quiet system without any interferences from other programs. It might help to use the '--warmup' or '--prepare' options.

Summary
  ./benchmarks-native.sh kotlin ran
    3.42 ± 0.08 times faster than ./benchmarks-jvm.sh kotlin
   59.35 ± 4.31 times faster than ./benchmarks-bun.sh kotlin
   62.59 ± 7.00 times faster than ./benchmarks-node.sh kotlin

monkey.kt on  new-gradle via 🅶 v8.7 via ☕ v21.0.5 via 🅺 took 53s
❯ hyperfine -w 3 './benchmarks-bun.sh vm-fast' './benchmarks-jvm.sh vm-fast' './benchmarks-native.sh vm-fast' './benchmarks-node.sh vm-fast' --export-json ../vm-fast.json
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

monkey.kt on  new-gradle via 🅶 v8.7 via ☕ v21.0.5 via 🅺 took 13m32s
❯ hyperfine -w 3 './benchmarks-bun.sh eval-fast' './benchmarks-jvm.sh eval-fast' './benchmarks-native.sh eval-fast' './benchmarks-node.sh eval-fast' --export-json ../eval-fast.json
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

```
❯ ruby --yjit plot.rb kotlin.json
                                 ┌                                                                                                    ┐
      ./benchmarks-bun.sh kotlin ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 1.7745110054200002
      ./benchmarks-jvm.sh kotlin ┤■■■■■ 0.10239241954068967
   ./benchmarks-native.sh kotlin ┤■ 0.02990045667510203
     ./benchmarks-node.sh kotlin ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 1.87144943592
                                 └                                                                                                    ┘

~/repositories via 💎 v3.3.6
❯ ruby --yjit plot.rb vm-fast.json
                                  ┌                                                                                                    ┐
      ./benchmarks-bun.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 23.2038023168
      ./benchmarks-jvm.sh vm-fast ┤■■■■■■■■■■■■■■■■■■ 4.9508734608
   ./benchmarks-native.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 12.111709727000001
     ./benchmarks-node.sh vm-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 22.323175457900003
                                  └                                                                                                    ┘

~/repositories via 💎 v3.3.6
❯ ruby --yjit plot.rb eval-fast.json
                                    ┌                                                                                                    ┐
      ./benchmarks-bun.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 34.86919739186
      ./benchmarks-jvm.sh eval-fast ┤■■■■■■■■■ 3.92911335406
   ./benchmarks-native.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 15.13231887616
     ./benchmarks-node.sh eval-fast ┤■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ 31.16230034386
                                    └                                                                                                    ┘
```
