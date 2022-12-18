package org.marioarias.monkey.objects

private fun argSizeCheck(expectedSize: Int, args: List<MObject?>, body: BuiltinFunction): MObject? {
    val length = args.size
    return if (length != expectedSize) {
        MError("wrong number of arguments. got=$length, want=$expectedSize")
    } else {
        body(args)
    }
}

private fun arrayCheck(builtinName: String, args: List<MObject?>, body: (MArray, Int) -> MObject?): MObject? {
    val first = args.first()
    return if (first !is MArray) {
        MError("argument to `$builtinName` must be ARRAY, got ${first.typeDesc()}")
    } else {
        body(first, first.elements.size)
    }
}

const val PUSH = "push"
const val REST = "rest"
const val FIRST = "first"
const val LAST = "last"

typealias Builtins = List<Pair<String, MBuiltinFunction>>

val builtins = listOf(
    "len" to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            when (val arg = it.first()) {
                is MString -> MInteger(arg.value.length.toLong())
                is MArray -> MInteger(arg.elements.size.toLong())
                else -> MError("argument to `len` not supported, got ${arg.typeDesc()}")
            }
        }
    },
    "puts" to MBuiltinFunction { args ->
        args.forEach { arg -> println(arg?.inspect()) }
        null
    },
    FIRST to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            arrayCheck(FIRST, it) { array, length ->
                if (length > 0) {
                    array.elements.first()!!
                } else {
                    null
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
                    null
                }
            }
        }
    },
    REST to MBuiltinFunction { args ->
        argSizeCheck(1, args) {
            arrayCheck(REST, it) { array, length ->
                if (length > 0) {
                    MArray(array.elements.drop(1))
                } else {
                    null
                }

            }
        }
        },
    PUSH to MBuiltinFunction { args ->
        argSizeCheck(2, args) {
            arrayCheck(PUSH, it) { array, _ ->
                MArray(array.elements + args[1])
            }
        }
    },
)

fun Builtins.getBuiltinByName(name: String): MBuiltinFunction? {
    return this.firstOrNull { (bName, _) -> bName == name }?.second
}
