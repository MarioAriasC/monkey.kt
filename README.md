# `monkey.kt`

Kotlin's implementation of the [Monkey Language](https://monkeylang.org/)

## Status

Currently, the first three chapters of the [interpreter book](https://interpreterbook.com/) have been implemented

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
    
## Run

For *nix systems, run the following command:

```shell
$ ./monkey.sh
```

## Test

For *nix systems, run the following command:

```shell
$ ./tests.sh
```