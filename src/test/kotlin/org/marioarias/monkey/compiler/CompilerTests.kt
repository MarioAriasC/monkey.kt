package org.marioarias.monkey.compiler

import org.marioarias.monkey.*
import org.marioarias.monkey.code.*
import org.marioarias.monkey.objects.MObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CompilerTests {
    data class CTC<T>(
        val input: String,
        val expectedConstants: List<T>,
        val expectedInstructions: List<Instructions>
    )


    @Test
    fun `integer arithmetic`() {
        listOf(
            CTC(
                "1 + 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpAdd),
                    make(OpPop)
                )
            ),
            CTC(
                "1; 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpPop),
                    make(OpConstant, 1),
                    make(OpPop)
                )
            ),
            CTC(
                "1 - 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpSub),
                    make(OpPop)
                )
            ),
            CTC(
                "1 * 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpMul),
                    make(OpPop)
                )
            ),
            CTC(
                "2 / 1",
                listOf(2, 1),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpDiv),
                    make(OpPop)
                )
            ),
            CTC(
                "-1",
                listOf(1),
                listOf(
                    make(OpConstant, 0),
                    make(OpMinus),
                    make(OpPop)
                )
            )
        ).runCompilerTests()
    }

    @Test
    fun `boolean expressions`() {
        listOf(
            CTC(
                "true",
                listOf(),
                listOf(
                    make(OpTrue),
                    make(OpPop)
                )
            ),
            CTC(
                "false",
                listOf(),
                listOf(
                    make(OpFalse),
                    make(OpPop)
                )
            ),
            CTC(
                "1 > 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpGreaterThan),
                    make(OpPop)
                )
            ),
            CTC(
                "1 < 2",
                listOf(2, 1),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpGreaterThan),
                    make(OpPop)
                )
            ),
            CTC(
                "1 == 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpEqual),
                    make(OpPop)
                )
            ),
            CTC(
                "1 != 2",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpNotEqual),
                    make(OpPop)
                )
            ),
            CTC(
                "true == false",
                listOf(),
                listOf(
                    make(OpTrue),
                    make(OpFalse),
                    make(OpEqual),
                    make(OpPop)
                )
            ),
            CTC(
                "true != false",
                listOf(),
                listOf(
                    make(OpTrue),
                    make(OpFalse),
                    make(OpNotEqual),
                    make(OpPop)
                )
            ),
            CTC(
                "!true",
                listOf(),
                listOf(
                    make(OpTrue),
                    make(OpBang),
                    make(OpPop)
                )
            )

        ).runCompilerTests()
    }

    @Test
    fun conditionals() {
        listOf(
            CTC(
                "if (true) {10}; 3333;",
                listOf(10, 3333),
                listOf(
                    make(OpTrue),
                    make(OpJumpNotTruthy, 10),
                    make(OpConstant, 0),
                    make(OpJump, 11),
                    make(OpNull),
                    make(OpPop),
                    make(OpConstant, 1),
                    make(OpPop)
                )
            ),
            CTC(
                "if (true) {10} else {20}; 3333;",
                listOf(10, 20, 3333),
                listOf(
                    make(OpTrue),
                    make(OpJumpNotTruthy, 10),
                    make(OpConstant, 0),
                    make(OpJump, 13),
                    make(OpConstant, 1),
                    make(OpPop),
                    make(OpConstant, 2),
                    make(OpPop)
                )
            )
        ).runCompilerTests()
    }

    @Test
    fun `global let statement`() {
        listOf(
            CTC(
                "let one = 1; let two = 2;",
                listOf(1, 2),
                listOf(
                    make(OpConstant, 0),
                    make(OpSetGlobal, 0),
                    make(OpConstant, 1),
                    make(OpSetGlobal, 1),
                )
            ),
            CTC(
                "let one = 1; one;",
                listOf(1),
                listOf(
                    make(OpConstant, 0),
                    make(OpSetGlobal, 0),
                    make(OpGetGlobal, 0),
                    make(OpPop),
                )
            ),
            CTC(
                "let one = 1; let two = one; two;",
                listOf(1),
                listOf(
                    make(OpConstant, 0),
                    make(OpSetGlobal, 0),
                    make(OpGetGlobal, 0),
                    make(OpSetGlobal, 1),
                    make(OpGetGlobal, 1),
                    make(OpPop),
                )
            )
        ).runCompilerTests()
    }

    @Test
    fun `string expressions`() {
        listOf(
            CTC(
                """"monkey"""",
                listOf("monkey"),
                listOf(
                    make(OpConstant, 0),
                    make(OpPop)
                )
            ),
            CTC(
                """"mon" + "key"""",
                listOf("mon", "key"),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpAdd),
                    make(OpPop)
                )
            )
        ).runCompilerTests()
    }

    @Test
    fun `array literals`() {
        listOf(
            CTC(
                "[]",
                listOf(),
                listOf(
                    make(OpArray, 0),
                    make(OpPop)
                )
            ),
            CTC(
                "[1, 2, 3]",
                listOf(1, 2, 3),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpConstant, 2),
                    make(OpArray, 3),
                    make(OpPop)
                )
            ),
            CTC(
                "[1 + 2, 3 - 4, 5 * 6]",
                listOf(1, 2, 3, 4, 5, 6),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpAdd),
                    make(OpConstant, 2),
                    make(OpConstant, 3),
                    make(OpSub),
                    make(OpConstant, 4),
                    make(OpConstant, 5),
                    make(OpMul),
                    make(OpArray, 3),
                    make(OpPop)
                )
            ),
        ).runCompilerTests()
    }

    @Test
    fun `hash literal`() {
        listOf(
            CTC(
                "{}",
                listOf(),
                listOf(
                    make(OpHash, 0),
                    make(OpPop)
                )
            ),
            CTC(
                "{1: 2, 3: 4, 5: 6}",
                listOf(1, 2, 3, 4, 5, 6),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpConstant, 2),
                    make(OpConstant, 3),
                    make(OpConstant, 4),
                    make(OpConstant, 5),
                    make(OpHash, 6),
                    make(OpPop)
                )
            ),
            CTC(
                "{1: 2 + 3, 4: 5 * 6}",
                listOf(1, 2, 3, 4, 5, 6),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpConstant, 2),
                    make(OpAdd),
                    make(OpConstant, 3),
                    make(OpConstant, 4),
                    make(OpConstant, 5),
                    make(OpMul),
                    make(OpHash, 4),
                    make(OpPop)
                )
            )
        ).runCompilerTests()
    }

    @Test
    fun `index expressions`() {
        listOf(
            CTC(
                "[1, 2, 3][1 + 1]",
                listOf(1, 2, 3, 1, 1),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpConstant, 2),
                    make(OpArray, 3),
                    make(OpConstant, 3),
                    make(OpConstant, 4),
                    make(OpAdd),
                    make(OpIndex),
                    make(OpPop)
                )
            ),
            CTC(
                "{1: 2}[2 - 1]",
                listOf(1, 2, 2, 1),
                listOf(
                    make(OpConstant, 0),
                    make(OpConstant, 1),
                    make(OpHash, 2),
                    make(OpConstant, 2),
                    make(OpConstant, 3),
                    make(OpSub),
                    make(OpIndex),
                    make(OpPop)
                )
            )
        ).runCompilerTests()
    }

    private fun <T> List<CTC<T>>.runCompilerTests() {
        forEach { (input, expectedConstants, expectedInstructions) ->
            println("input = ${input}")
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
                is String -> testStringObject(constant, actual[i])
            }
        }
    }
}