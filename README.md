# `monkey.kt`

Kotlin's implementation of the [Monkey Language](https://monkeylang.org/)

## Articles

https://medium.com/@mario.arias.c/comparing-kotlin-and-golang-implementations-of-the-monkey-language-3a41122ea732

https://medium.com/@mario.arias.c/comparing-kotlin-and-go-implementations-of-the-monkey-language-ii-raiders-of-the-lost-performance-b9aa09945281

## Status

The two books ([Writing An Interpreter In Go](https://interpreterbook.com/) and [Writing A Compiler in Go](https://compilerbook.com/)) are implemented.

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

For an implementation of the interpreter with macros (but not a compiler) check the branch [eval-macros](https://github.com/MarioAriasC/monkey.kt/tree/eval-macros)
    
## Run
                                                  
### JVM

For *nix systems, run the following command:

```shell
$ ./monkey.sh
```

### GraalVM Native Image

To run the application with [GraalVM](https://www.graalvm.org/) Native Image, you need to follow certain steps:

 - Install GraalVM, I recommend using [SDKMAN](https://sdkman.io/) (Not just for GraalVM but for any JVM tool in general)
 - Install the `native-image` [executable](https://www.graalvm.org/reference-manual/native-image/#install-native-image) 
 - Create a GRAALVM_HOME environment variable. On *nix systems `export GRAALVM_HOME="$HOME/.sdkman/candidates/java/21.2.0.r11-grl/` or your equivalent GraalVM location
 - Run the command
```shell
$ ./gradlew clean nativeImage
```

# Benchmarks

To run the standard Monkey language benchmarks (`fibonacci(35);`) add a parameter `vm`, `vm-fast`, `eval` or `eval-fast` to the script.

Example

```shell
$ ./monkey.sh vm
engine=vm, result=9227465, duration=7.516433414s
```

```shell
$ ./monkey.sh eval
engine=eval, result=9227465, duration=11.931564613s
```
 
All the benchmarks tested on a MBP 15-inch 2019. Intel Core i9 2.3Ghz 8-Core, 32 GB 2400 MHZ DDR4
 
| Environment | Eval | VM |
|---|---|---|
|openjdk 11.0.12 Zulu| 11.93s | 7.51s |
|openjdk 11.0.12 GraalVM| 7.29s | 6.82s |
|GraalVM Native Image | 21.06s | 20.06s |



## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```