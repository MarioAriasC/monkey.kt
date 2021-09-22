# `monkey.kt`

Kotlin's implementation of the [Monkey Language](https://monkeylang.org/)

## Articles

https://medium.com/@mario.arias.c/comparing-kotlin-and-golang-implementations-of-the-monkey-language-3a41122ea732

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

Due to the use of reflection by Kotlin, GraalVM will create a fallback image (An image that use JVM to run the reflection bits).
If you want to build a pure native image you can go to the file [built.gradle.kts](build.gradle.kts) and uncomment these lines (starting from `arguments(`)

```kotlin
nativeImage {
    graalVmHome = System.getenv("GRAALVM_HOME")
    buildType { build ->
        build.executable("org.marioarias.monkey.MainKt")
    }
    executableName = "monkey-grl"
    outputDirectory = file(".")
/*    arguments(
        "--no-fallback"
//      this option is equivalent to --no-fallback        
//      "-H:ReflectionConfigurationFiles=./graal-reflect.json" 
    )*/
}
```

Basically, We're adding the option `--no-fallback`. With this option, GraalVM will try to replace reflection with static calls.

# Benchmarks

To run the standard Monkey language benchmarks (`fibonacci(35);`) add a parameter `vm` or `eval` to the script.

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
|openjdk 11.0.12 Zulu| 11.93ms | 7.51ms |
|openjdk 11.0.12 GraalVM| 7.29ms | 6.82ms |
|GraalVM Native Image with JVM fallback| 7.97ms | 7.22ms |
|GraalVM Native Image | 20.63ms | 23.89ms |



## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```