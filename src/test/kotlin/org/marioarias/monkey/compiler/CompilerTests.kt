package org.marioarias.monkey.compiler

import org.marioarias.monkey.*
import org.marioarias.monkey.code.*
import org.marioarias.monkey.objects.MCompiledFunction
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.typeDesc
import org.testng.annotations.Test
//import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class CompilerTests {
    data class CTC<T>(
        val input: String,
        val expectedConstants: List<T>,
        val expectedInstructions: List<Instructions>
    ) {
        constructor(input: String, expectedConstants: List<T>, vararg instructions: Instructions) : this(
            input,
            expectedConstants,
            listOf(*instructions)
        )
    }


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

    @Test
    fun functions() {
        listOf(
            CTC(
                "fn() {return 5 + 10 }",
                listOf(
                    5, 10, listOf(
                        make(OpConstant, 0),
                        make(OpConstant, 1),
                        make(OpAdd),
                        make(OpReturnValue)
                    )
                ),
                listOf(
                    make(OpConstant, 2),
                    make(OpPop)
                )
            ),
            CTC(
                "fn() { 5 + 10 }",
                listOf(
                    5, 10, listOf(
                        make(OpConstant, 0),
                        make(OpConstant, 1),
                        make(OpAdd),
                        make(OpReturnValue)
                    )
                ),
                listOf(
                    make(OpConstant, 2),
                    make(OpPop)
                )
            ),
            CTC(
                "fn() {1; 2}",
                listOf(
                    1, 2, listOf(
                        make(OpConstant, 0),
                        make(OpPop),
                        make(OpConstant, 1),
                        make(OpReturnValue),
                    )
                ),
                listOf(
                    make(OpConstant, 2),
                    make(OpPop),
                ),
            )
        ).runCompilerTests()
    }

    @Test
    fun `functions  without return value`() {
        listOf(
            CTC(
                "fn () {}",
                listOf(
                    listOf(
                        make(OpReturn)
                    )
                ),
                listOf(
                    make(OpConstant, 0),
                    make(OpPop)
                ),
            )
        ).runCompilerTests()
    }

    @Test
    fun `function calls`() {
        listOf(
            CTC(
                "fn(){24}();",
                listOf(
                    24,
                    listOf(
                        make(OpConstant, 0),
                        make(OpReturnValue)
                    )
                ),
                make(OpConstant, 1),
                make(OpCall, 0),
                make(OpPop),
            ),
            CTC(
                """
let noArg = fn() { 24 };
noArg();                
            """.trimIndent(),
                listOf(
                    24,
                    listOf(
                        make(OpConstant, 0),
                        make(OpReturnValue)
                    )
                ),
                make(OpConstant, 1),
                make(OpSetGlobal, 0),
                make(OpGetGlobal, 0),
                make(OpCall, 0),
                make(OpPop),
            ),
            CTC(
                """
let oneArg = fn(a) {};
oneArg(24);                    
                """.trimIndent(),
                listOf(
                    listOf(
                        make(OpReturn)
                    ),
                    24
                ),
                make(OpConstant, 0),
                make(OpSetGlobal, 0),
                make(OpGetGlobal, 0),
                make(OpConstant, 1),
                make(OpCall, 1),
                make(OpPop),
            ),
            CTC(
                """
let manyArg = fn(a, b, c){};
manyArg(24, 25, 26);                    
                """.trimIndent(),
                listOf(
                    instructions(
                        make(OpReturn)
                    ),
                    24, 25, 26
                ),
                make(OpConstant, 0),
                make(OpSetGlobal, 0),
                make(OpGetGlobal, 0),
                make(OpConstant, 1),
                make(OpConstant, 2),
                make(OpConstant, 3),
                make(OpCall, 3),
                make(OpPop),
            ),
            CTC(
                """
let oneArg = fn(a) {a};
oneArg(24);                    
                """.trimIndent(),
                listOf(
                    instructions(
                        make(OpGetLocal, 0),
                        make(OpReturnValue)
                    ), 24
                ),
                make(OpConstant, 0),
                make(OpSetGlobal, 0),
                make(OpGetGlobal, 0),
                make(OpConstant, 1),
                make(OpCall, 1),
                make(OpPop),
            ),
            CTC(
                """
let manyArgs = fn(a, b, c) {a; b; c};
manyArgs(24, 25, 26);                    
                """.trimIndent(),
                listOf(
                    instructions(
                        make(OpGetLocal, 0),
                        make(OpPop),
                        make(OpGetLocal, 1),
                        make(OpPop),
                        make(OpGetLocal, 2),
                        make(OpReturnValue),
                    ), 24, 25, 26
                ),
                make(OpConstant, 0),
                make(OpSetGlobal, 0),
                make(OpGetGlobal, 0),
                make(OpConstant, 1),
                make(OpConstant, 2),
                make(OpConstant, 3),
                make(OpCall, 3),
                make(OpPop),
            )
        ).runCompilerTests()

    }

    @Test
    fun `compiler scopes`() {
        val compiler = MCompiler()
        testScopeIndexSize(compiler, 0)
        val globalSymbolTable = compiler.symbolTable
        compiler.emit(OpMul)

        compiler.enterScope()
        testScopeIndexSize(compiler, 1)

        compiler.emit(OpSub)

        testScopeInstructionsSize(compiler, 1)

        var last = compiler.currentScope().lastInstruction
        assertEquals(OpSub, last.op)

        assertEquals(globalSymbolTable, compiler.symbolTable.outer)

        compiler.leaveScope()
        testScopeIndexSize(compiler, 0)

        assertEquals(globalSymbolTable, compiler.symbolTable)

        assertNull(compiler.symbolTable.outer)

        compiler.emit(OpAdd)

        testScopeInstructionsSize(compiler, 2)

        last = compiler.currentScope().lastInstruction
        assertEquals(OpAdd, last.op)

        val previous = compiler.currentScope().previousInstruction
        assertEquals(OpMul, previous.op)
    }

    @Test
    fun `let statements scopes`() {
        listOf(
            CTC(
                """
let num = 55;
fn() { num }                
            """.trimIndent(),
                listOf(
                    55, instructions(
                        make(OpGetGlobal, 0),
                        make(OpReturnValue),
                    )
                ),
                make(OpConstant, 0),
                make(OpSetGlobal, 0),
                make(OpConstant, 1),
                make(
                    OpPop
                ),
            ),
            CTC(
                """
            fn() {
            	let num = 55;
            	num
            }                        
                                """.trimIndent(),
                listOf(
                    55,
                    instructions(
                        make(OpConstant, 0),
                        make(OpSetLocal, 0),
                        make(OpGetLocal, 0),
                        make(OpReturnValue),
                    )
                ),
                make(OpConstant, 1),
                make(OpPop),
            ),
            CTC(
                """
fn() {
	let a = 55;
	let b = 77;
	a + b;
}                                        
                """.trimIndent(),
                listOf(
                    55, 77, instructions(
                        make(OpConstant, 0),
                        make(OpSetLocal, 0),
                        make(OpConstant, 1),
                        make(OpSetLocal, 1),
                        make(OpGetLocal, 0),
                        make(OpGetLocal, 1),
                        make(OpAdd),
                        make(OpReturnValue),
                    )
                ),
                make(OpConstant, 2),
                make(OpPop),
            )
        ).runCompilerTests()
    }

    @Test
    fun builtins() {
        listOf(
            CTC(
                """
len([]);
push([], 1);                
            """.trimIndent(),
                listOf(1),
                make(OpGetBuiltin, 0),
                make(OpArray, 0),
                make(OpCall, 1),
                make(OpPop),
                make(OpGetBuiltin, 5),
                make(OpArray, 0),
                make(OpConstant, 0),
                make(OpCall, 2),
                make(OpPop),
            ),
            CTC(
                "fn() { len([])}",
                listOf(
                    instructions(
                        make(OpGetBuiltin, 0),
                        make(OpArray, 0),
                        make(OpCall, 1),
                        make(OpReturnValue)
                    )
                ),
                make(OpConstant, 0),
                make(OpPop),
            )
        ).runCompilerTests()
    }

    private fun testScopeInstructionsSize(compiler: MCompiler, instructionsSize: Int) {
        assertEquals(instructionsSize, compiler.currentScope().instructions.size)
    }

    private fun testScopeIndexSize(compiler: MCompiler, scopeIndex: Int) {
        assertEquals(scopeIndex, compiler.scopeIndex)
    }

    private fun <T> List<CTC<out T>>.runCompilerTests() {
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
        val concatenated = expected.concat()
        assertEquals(concatenated.size, actual.size)
        concatenated.assertEquals(actual)
    }


    private fun <T> testConstants(expected: List<T>, actual: List<MObject>) {
        assertEquals(expected.size, actual.size)
        expected.forEachIndexed { i, constant ->
            when (constant) {
                is Long -> testIntegerObject(constant, actual[i])
                is String -> testStringObject(constant, actual[i])
                is List<*> -> {
                    when (val act = actual[i]) {
                        is MCompiledFunction -> {
                            @Suppress("UNCHECKED_CAST")
                            testInstructions(constant as List<Instructions>, act.instructions)
                        }
                        else -> fail("constant $act - not a function, got = ${act.typeDesc()}")
                    }

                }
            }
        }
    }
}