package org.marioarias.monkey.wasm

import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

@WasmImport("wasi_snapshot_preview1", "args_sizes_get")
private external fun argsSizeGet(argCounterPtr: UInt, bufferSizePtr: UInt): Int

@WasmImport("wasi_snapshot_preview1", "args_get")
private external fun argsGet(argvPtr: UInt, argvBufPtr: UInt): Int

@OptIn(UnsafeWasmMemoryApi::class)
internal fun argv(): Array<String> {
    val argCounter: Int
    val bufferSize: Int
    withScopedMemoryAllocator { allocator ->
        val argCounterPtr = allocator.allocate(Int.SIZE_BYTES)
        val bufferSizePtr = allocator.allocate(Int.SIZE_BYTES)
        val errno = argsSizeGet(argCounterPtr.address, bufferSizePtr.address)
        check(errno == 0) { "args_size_get: $errno" }
        argCounter = argCounterPtr.loadInt()
        bufferSize = bufferSizePtr.loadInt()
    }
    val buffer = ByteArray(bufferSize)
    return withScopedMemoryAllocator { allocator ->
        val argvPtr = allocator.allocate(argCounter * Int.SIZE_BYTES)
        val errno = argsGet(argvPtr.address, allocator.allocate(bufferSize).address)
        check(errno == 0) { "args_get $errno" }
        Array(argCounter) { a ->
            val argPtr = Pointer(argvPtr.plus(a * Int.SIZE_BYTES).loadInt().toUInt())
            for (i in buffer.indices) {
                buffer[i] = argPtr.plus(i).loadByte()
                if (buffer[i] == 0.toByte()) {
                    return@Array buffer.decodeToString(endIndex = i)
                }
            }
            error("missing \\0")
        }
    }
}
