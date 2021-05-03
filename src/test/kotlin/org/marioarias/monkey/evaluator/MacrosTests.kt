package org.marioarias.monkey.evaluator

import org.marioarias.monkey.ast.Program
import org.marioarias.monkey.evaluator.Environment.Companion.newEnvironment
import org.marioarias.monkey.lexer.Lexer
import org.marioarias.monkey.objects.MMacro
import org.marioarias.monkey.parser.Parser
import org.testng.Assert
import org.testng.annotations.Test


class MacrosTests {
    @Test
    fun `define macros`() {
        val input = """let number = 1;
        let function = fn(x, y){x + y};
        let myMacro = macro(x, y){x + y;};""".trimIndent()

        val env = newEnvironment()

        val program = defineMacros(testParseProgram(input), env)

        Assert.assertEquals(program.statements.size, 2)

        Assert.assertNull(env["number"])

        Assert.assertNull(env["function"])

        val obj = env["myMacro"]
        Assert.assertNotNull(obj)

        when (obj) {
            is MMacro -> {
                Assert.assertEquals(obj.parameters?.size, 2)
                Assert.assertEquals(obj.parameters!![0].toString(), "x")
                Assert.assertEquals(obj.parameters!![1].toString(), "y")
                Assert.assertEquals(obj.body.toString(), "(x + y)")
            }
            else -> Assert.fail("object is not MMacro. got ${obj?.type()}")
        }

    }

    @Test
    fun `expand macros`() {
        data class TestData(val input: String, val expected: String)
        listOf(
            TestData(
                """
            |let infixExpression = macro(){quote(1 + 2);};
            |infixExpression();
        """.trimMargin(), "(1 + 2)"
            ),
            TestData(
                """
                let reverse = macro(a, b) {quote(unquote(b) - unquote(a));}
                reverse(2 + 2, 10 - 5);
                    """.trimMargin(), "(10 - 5) - (2 + 2)"
            ),
            TestData(
                """
                let unless = macro(condition, consequence, alternative) {
                	quote(if (!(unquote(condition))) {
                		unquote(consequence);
                	} else {
                		unquote(alternative);
                	});
                }
                unless(10 > 5, puts("not greater"), puts("greater"));
                """.trimMargin(), """if (!(10 > 5)) {puts("not greater")} else {puts("greater")}"""
                        )
        ).forEach { (input, expected) ->
            val expectedProgram = testParseProgram(expected)
            val env = newEnvironment()
            val program = defineMacros(testParseProgram(input), env)
            val expanded = expandMacros(program, env)
            Assert.assertEquals(expanded.toString(), expectedProgram.toString())
        }
    }

    private fun testParseProgram(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        return parser.parseProgram()
    }
}