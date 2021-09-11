package org.marioarias.monkey.evaluator

import org.marioarias.monkey.objects.getBuiltinByName
import org.marioarias.monkey.objects.builtins as objectBuiltins

const val PUSH = "push"
const val REST = "rest"
const val FIRST = "first"
const val LAST = "last"

internal val builtins = mapOf(
    "len" to objectBuiltins.getBuiltinByName("len"),
    PUSH to objectBuiltins.getBuiltinByName(PUSH),
    REST to objectBuiltins.getBuiltinByName(REST),
    FIRST to objectBuiltins.getBuiltinByName(FIRST),
    LAST to objectBuiltins.getBuiltinByName(LAST),
    "puts" to objectBuiltins.getBuiltinByName("puts")
)