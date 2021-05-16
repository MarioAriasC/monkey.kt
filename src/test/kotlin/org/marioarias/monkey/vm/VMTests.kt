package org.marioarias.monkey.vm

import org.marioarias.monkey.parse
import org.marioarias.monkey.compiler.MCompiler
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.testIntegerObject
import kotlin.test.Test

class VMTests {
    data class VMTestCase<T>(val input: String, val expected: T)

    @Test
    fun `integer arithmetic`() {
        listOf(
            VMTestCase("1", 1),
            VMTestCase("2", 2),
            VMTestCase("1 + 2", 3),
        ).runVmTests()
    }

    private fun <T> List<VMTestCase<T>>.runVmTests() {
        forEach { (input, expected) ->
            val program = parse(input)
            val compiler = MCompiler()
            compiler.compile(program)
            val vm = VM(compiler.bytecode())
            vm.run()
            val stackElem = vm.stackTop()!!
            testExpectObject(expected, stackElem)
        }
    }

    private fun <T> testExpectObject(expected: T, actual: MObject) {
        when(expected){
            is Long -> testIntegerObject(expected, actual)
        }
    }
}