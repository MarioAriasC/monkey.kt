package org.marioarias.monkey.vm

import org.marioarias.monkey.checkType
import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.objects.*
import org.marioarias.monkey.parse
import org.marioarias.monkey.testIntegerObject
import org.marioarias.monkey.testStringObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VMTests {
    data class VTC<T>(val input: String, val expected: T)

    @Test
    fun `integer arithmetic`() {
        listOf(
            VTC("1", 1),
            VTC("2", 2),
            VTC("1 + 2", 3),
            VTC("1 - 2", -1),
            VTC("1 * 2", 2),
            VTC("4 / 2", 2),
            VTC("50 / 2 * 2 + 10 - 5", 55),
            VTC("5 + 5 + 5 + 5 - 10", 10),
            VTC("2 * 2 * 2 * 2 * 2", 32),
            VTC("5 * 2 + 10", 20),
            VTC("5 + 2 * 10", 25),
            VTC("5 * (2 + 10)", 60),
            VTC("-5", -5),
            VTC("-10", -10),
            VTC("-50 + 100 + - 50", 0),
            VTC("(5 + 10 * 2 + 15  / 3) * 2 + - 10", 50),
        ).runVmTests()
    }

    @Test
    fun `boolean expression`() {
        listOf(
            VTC("true", true),
            VTC("false", false),
            VTC("1 < 2", true),
            VTC("1 > 2", false),
            VTC("1 > 1", false),
            VTC("1 == 1", true),
            VTC("1 != 1", false),
            VTC("1 == 2", false),
            VTC("1 != 2", true),
            VTC("true == true", true),
            VTC("false == false", true),
            VTC("true == false", false),
            VTC("true != false", true),
            VTC("false != true", true),
            VTC("(1 < 2) == true", true),
            VTC("(1 < 2) == false", false),
            VTC("(1 > 2) == true", false),
            VTC("(1 > 2) == false", true),
            VTC("!true", false),
            VTC("!false", true),
            VTC("!5", false),
            VTC("!!true", true),
            VTC("!!false", false),
            VTC("!!5", true),
            VTC("!(if (false) { 5; })", true),
        ).runVmTests()
    }

    @Test
    fun conditional() {
        listOf<VTC<Any>>(
            VTC("if (true) {10}", 10),
            VTC("if (true) {10} else {20}", 10),
            VTC("if (false) {10} else {20}", 20),
            VTC("if (1) {10}", 10),
            VTC("if (1 < 2) {10}", 10),
            VTC("if (1 < 2) {10} else {20}", 10),
            VTC("if (1 > 2) {10} else {20}", 20),
            VTC("if (1 > 2) {10}", Null),
            VTC("if (false) {10}", Null),
            VTC("if ((if (false) {10})) {10} else {20}", 20),
        ).runVmTests()
    }

    @Test
    fun `global let statements`() {
        listOf(
            VTC("let one = 1; one;", 1),
            VTC("let one = 1; let two = 2; one + two", 3),
            VTC("let one = 1; let two = one + one; one + two", 3),
        ).runVmTests()
    }

    @Test
    fun `string expressions`() {
        listOf(
            VTC(""" "monkey" """, "monkey"),
            VTC(""" "mon" + "key" """, "monkey"),
            VTC(""" "mon" + "key" + "banana" """, "monkeybanana"),
        ).runVmTests()
    }

    @Test
    fun `array literals`() {
        listOf(
            VTC("[]", emptyList()),
            VTC("[1, 2, 3]", listOf(1L, 2L, 3L)),
            VTC("[1 + 2, 3 * 4, 5 + 6]", listOf(3L, 12L, 11L)),
        ).runVmTests()
    }

    @Test
    fun `hash literals`() {
        listOf(
            VTC("{}", emptyMap()),
            VTC(
                "{1: 2, 2: 3}", mapOf(
                    MInteger(1).hashKey() to 2L,
                    MInteger(2).hashKey() to 3L
                )
            ),
            VTC(
                "{1 + 1: 2 * 2, 3 + 3: 4 * 4}", mapOf(
                    MInteger(2).hashKey() to 4L,
                    MInteger(6).hashKey() to 16L
                )
            ),
        ).runVmTests()
    }

    @Test
    fun `index expressions`() {
        listOf(
            VTC("[1, 2, 3][1]", 2),
            VTC("[1, 2, 3][0 + 2]", 3),
            VTC("[[1, 1, 1]][0][0]", 1),
            VTC("[][0]", Null),
            VTC("[1, 2, 3][99]", Null),
            VTC("[1][-1]", Null),
            VTC("{1: 1, 2: 2}[1]", 1),
            VTC("{1: 1, 2: 2}[2]", 2),
            VTC("{1: 1}[0]", Null),
            VTC("{}[0]", Null),
        ).runVmTests()
    }

    private fun <T> List<VTC<out T>>.runVmTests() {
        forEach { (input, expected) ->
            val program = parse(input)
            val compiler = MCompiler()
            compiler.compile(program)
            val vm = VM(compiler.bytecode())
            vm.run()
            val stackElem = vm.lastPoppedStackElem()!!
            testExpectObject(expected, stackElem)
        }
    }

    private fun <T> testExpectObject(expected: T, actual: MObject) {
        when (expected) {
            is Long -> testIntegerObject(expected, actual)
            is Boolean -> testBooleanObject(expected, actual)
            is MNull -> assertEquals(MNull, actual, "object is not Null")
            is String -> testStringObject(expected, actual)
            is List<*> -> {
                checkType(actual) { array: MArray ->
                    assertEquals(array.elements.size, expected.size)
                    expected.forEachIndexed { i, any ->
                        testIntegerObject(any as Long, array.elements[i]!!)
                    }
                }
            }
            is Map<*, *> -> {
                checkType(actual) { hash: MHash ->
                    assertEquals(expected.size, hash.pairs.size)
                    expected.forEach { (expectedKey, expectedValue) ->
                        val pair = hash.pairs[expectedKey]
                        assertNotNull(pair, "no pair for give key in pairs")
                        testIntegerObject(expectedValue as Long, pair.value)
                    }
                }
            }
        }
    }

    private fun testBooleanObject(expected: Boolean, actual: MObject) {
        val value = (actual as MBoolean).value
        assertEquals(expected, value, "object has wrong value. got=$value, want=$expected")
    }
}