package org.marioarias.monkey.evaluator

import org.marioarias.monkey.objects.*


private fun argSizeCheck(expectedSize: Int, args: List<MObject?>, body: BuiltinFunction): MObject {
    val length = args.size
    return if (length != expectedSize) {
        MError("wrong number of arguments. got=$length, want=1")
    } else {
        body(args)
    }
}

private fun arrayCheck(builtinName: String, args: List<MObject?>, body: (MArray, Int) -> MObject): MObject {
    return if (args.first()?.type() != ObjectType.ARRAY) {
        MError("argument to `$builtinName` must be ARRAY, got ${args.first()?.type()}")
    } else {
        val array = args.first() as MArray
        body(array, array.elements.size)
    }
}

const val PUSH = "push"
const val REST = "rest"
const val FIRST = "first"
const val LAST = "last"

val builtins = mapOf(
    "len" to MBuiltinFunction { args ->

        argSizeCheck(1, args) {
            when (val arg = it.first()) {
                is MString -> MInteger(arg.value.length.toLong())
                is MArray -> MInteger(arg.elements.size.toLong())
                else -> MError("argument to `len` not supported, got ${arg?.type()}")
            }
        }
    },
    "push" to MBuiltinFunction { args ->
        args.forEach { arg -> println(arg?.inspect()) }
        MNull
    },
    PUSH to MBuiltinFunction { args ->
        argSizeCheck(2, args) {
            arrayCheck(PUSH, it) { array, _ ->
                MArray(array.elements + args[1])
            }
        }
    },
    REST to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            arrayCheck(REST, it) { array, length ->
                if (length > 0) {
                    MArray(array.elements.drop(1))
                } else {
                    MNull
                }

            }
        }
    },
    FIRST to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            arrayCheck(FIRST, it) { array, length ->
                if (length > 0) {
                    array.elements.first()!!
                } else {
                    MNull
                }
            }
        }
    },
    LAST to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            arrayCheck(LAST, it) { array, length ->
                if (length > 0) {
                    array.elements.last()!!
                } else {
                    MNull
                }
            }
        }
    }
)