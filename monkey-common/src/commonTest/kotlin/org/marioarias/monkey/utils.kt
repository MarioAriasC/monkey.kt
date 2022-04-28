@file:OptIn(ExperimentalUnsignedTypes::class)
package org.marioarias.monkey

import org.marioarias.monkey.ast.Program
import org.marioarias.monkey.code.Instructions
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.MString
import org.marioarias.monkey.objects.typeDesc
import org.marioarias.monkey.parser.Parser
import kotlin.test.assertEquals
import kotlin.test.fail

inline fun <reified T> checkType(value: Any?, body: (T) -> Unit) = when (value) {
    is T -> body(value)
    else -> fail("$value is not ${T::class.simpleName}. got=${value!!::class.simpleName}")
}

@Deprecated("use checkType", ReplaceWith("checkType"))
inline fun <reified T> isType(value: Any?, body: (T) -> Boolean): Boolean {
    return when (value) {
        is T -> {
            body(value)
        }
        else -> {
            fail("$value is not ${T::class.simpleName}. got=${value!!::class.simpleName}")
        }
    }
}

fun parse(input: String): Program {
    return Parser(Lexer(input)).parseProgram()
}

fun List<Instructions>.concat(): Instructions = fold(ubyteArrayOf()) { acc, bytes -> acc + bytes }

fun Instructions.assertEquals(actual: Instructions) {
    forEachIndexed { i, byte ->
        assertEquals(byte, actual[i])
    }
}

fun testIntegerObject(expected: Long, actual: MObject) {
    when (actual) {
        is MInteger -> assertEquals(expected, actual.value)
        else -> fail("object is  not Integer. got=${actual.typeDesc()}")
    }
}

fun testStringObject(expected: String, actual: MObject) {
    when (actual) {
        is MString -> assertEquals(expected, actual.value)
        else -> fail("object is not String. got${actual.typeDesc()}")
    }
}