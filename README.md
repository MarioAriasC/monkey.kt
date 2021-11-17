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

There are three different executable environments, JVM, GraalVM Native Image and Native. Each executable has 3 different
shell scripts, `build`, `repl`, `benchmarks`

| Executable environment | Build | REPL | Benchmarks
|---|---|---|---|
|JVM|`build-jvm.sh`|`repl-jvm.sh`|`benchmarks-jvm.sh`|
|Graal|`build-graal.sh`|`repl-graal.sh`|`benchmarks-graal.sh`|
|Native|`build-native.sh`|`repl-native.sh`|`benchmarks-native.sh`|

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

**WARNING:** The execution with Kotlin native is failing with a Segmentation Fault error. Once I managed to fix it I'll delete this note.

For *nix systems, run the following command:

```shell
$ ./build-native.sh
```

And then:

```shell
$ ./repl-native.sh
```

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
