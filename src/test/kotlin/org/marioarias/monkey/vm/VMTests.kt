package org.marioarias.monkey.vm

import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.objects.MBoolean
import org.marioarias.monkey.objects.MNull
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.parse
import org.marioarias.monkey.testIntegerObject
import kotlin.test.Test
import kotlin.test.assertEquals

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

    private fun <T> List<VTC<T>>.runVmTests() {
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
        }
    }

    private fun testBooleanObject(expected: Boolean, actual: MObject) {
        val value = (actual as MBoolean).value
        assertEquals(expected, value, "object has wrong value. got=$value, want=$expected")
    }
}