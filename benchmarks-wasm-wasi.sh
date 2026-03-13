#!/usr/bin/env bash
node --experimental-wasm-exnref  ./monkey-wasm-wasi/build/compileSync/wasmWasi/main/productionExecutable/optimized/monkey.kt-monkey-wasm-wasi.mjs "$1"