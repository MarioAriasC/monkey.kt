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

For *nix systems, run the following command:

```shell
$ ./monkey.sh
```

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


## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```