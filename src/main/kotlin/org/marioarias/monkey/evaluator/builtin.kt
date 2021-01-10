package org.marioarias.monkey.evaluator

import org.marioarias.monkey.objects.MBuiltinFunction
import org.marioarias.monkey.objects.MError
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MString

val builtins = mapOf(
    "len" to MBuiltinFunction { args ->
        val length = args.size
        if (length != 1) {
            MError("wrong number of arguments. got=$length, want=1")
        } else {
            when (val arg = args.first()) {
                is MString -> MInteger(arg.value.length.toLong())
                else -> MError("argument to `len` not supported, got ${arg?.type()}")
            }

        }
    })