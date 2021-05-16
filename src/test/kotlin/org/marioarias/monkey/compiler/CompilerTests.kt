package org.marioarias.monkey.compiler

import org.marioarias.monkey.assertEquals
import org.marioarias.monkey.code.Instructions
import org.marioarias.monkey.code.OpAdd
import org.marioarias.monkey.code.OpConstant
import org.marioarias.monkey.code.make
import org.marioarias.monkey.concat
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.parse
import org.marioarias.monkey.testIntegerObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CompilerTests {
    data class CompilerTestCase<T>(
        val input: String,
        val expectedConstants: List<T>,
        val expectedInstructions: List<Instructions>
    )

    @Test
    fun `integer arithmetic`() {
        listOf(CompilerTestCase(
            "1 + 2",
            listOf(1, 2),
            listOf(
                make(OpConstant, 0),
                make(OpConstant, 1),
                make(OpAdd)
            )
        )).runCompilerTests()
    }

    private fun <T> List<CompilerTestCase<T>>.runCompilerTests() {
        forEach { (input, expectedConstants, expectedInstructions) ->
            val program = parse(input)
            val compiler = MCompiler()

            try {
                compiler.compile(program)
                val bytecode = compiler.bytecode()
                testInstructions(expectedInstructions, bytecode.instructions)
                testConstants(expectedConstants, bytecode.constants)
            } catch (e: MCompilerException) {
                fail("compiler error: ${e.message}", e)
            }

        }
    }

    private fun testInstructions(expected: List<Instructions>, actual: Instructions) {
        val concatted = expected.concat()
        assertEquals(concatted.size, actual.size)
        concatted.assertEquals(actual)
    }



    private fun <T> testConstants(expected: List<T>, actual: List<MObject>) {
        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { i, constant ->
            when (constant) {
                is Long -> testIntegerObject(constant, actual[i])
            }
        }
    }
}