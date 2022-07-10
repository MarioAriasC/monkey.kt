package org.marioarias.monkey.parser

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.checkType
import org.marioarias.monkey.lexer.Lexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail


class ParserTests {

    @Test
    fun `let statements`() {
        val tests = listOf(
            Triple("let x = 5;", "x", 5),
            Triple("let y = true;", "y", true),
            Triple("let foobar = y;", "foobar", "y")
        )

        tests.forEach { (input, expectedIdentifier, expectedValue) ->
            val program = createProgram(input)

            countStatements(1, program)

            val statement = program.statements.first()

            testLetStatement(statement, expectedIdentifier)

            val value = (statement as LetStatement).value
            testLiteralExpression(value, expectedValue)
        }
    }

    @Test
    fun `return statements`() {
        val tests = listOf(
            "return 5;" to 5,
            "return true;" to true,
            "return foobar;" to "foobar"
        )

        tests.forEach { (input, expectedValue) ->
            val program = createProgram(input)

            countStatements(1, program)

            checkType(program.statements.first()) { statement: ReturnStatement ->
                assertEquals(
                    "return",
                    statement.tokenLiteral(),
                    "statement.tokenLiteral() not 'return', got=${statement.tokenLiteral()}"
                )
                testLiteralExpression(statement.returnValue, expectedValue)
            }
        }
    }

    @Test
    fun `identifier expressions`() {
        val input = "foobar;"

        val program = createProgram(input)

        countStatements(1, program)


        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { identifier: Identifier ->
                assertEquals("foobar", identifier.value, "identifier.value not 'foobar'. got=${identifier.value}")
                assertEquals(
                    "foobar",
                    identifier.tokenLiteral(),
                    "identifier.tokenLiteral() not 'foobar'. got=${identifier.tokenLiteral()}"
                )
            }

        }
    }

    @Test
    fun `integer literals`() {
        val input = "5;"

        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            when (val literal = statement.expression) {
                is IntegerLiteral -> {
                    assertEquals(5.toLong(), literal.value, "identifier.value not 5. got=${literal.value}")
                    assertEquals(
                        "5",
                        literal.tokenLiteral(),
                        "identifier.tokenLiteral() not '5'. got=${literal.tokenLiteral()}"
                    )
                }
                else -> {
                    fail("statement.expression not IntegerLiteral. got=${literal!!::class.simpleName}")
                }
            }
        }
    }

    @Test
    fun `parsing prefix expressions`() {      
        val tests = listOf(
            Triple("!5;", "!", 5),
            Triple("-15;", "-", 15),
            Triple("!true;", "!", true),
            Triple("!false;", "!", false),
        )

        tests.forEach { (input, operator, value) ->
            val program = createProgram(input)

            countStatements(1, program)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { expression: PrefixExpression ->
                    assertEquals(
                        operator,
                        expression.operator,
                        "expression.operator is not ${operator}. got=${expression.operator}"
                    )
                    testLiteralExpression(expression.right, value)
                }
            }
        }
    }

    @Test
    fun `parsing infix expressions`() {
        data class TestData<L, R>(val input: String, val leftValue: L, val operator: String, val rightValue: R)

        val tests = listOf(
            TestData("5 + 5;", 5, "+", 5),
            TestData("5 - 5;", 5, "-", 5),
            TestData("5 * 5;", 5, "*", 5),
            TestData("5 / 5;", 5, "/", 5),
            TestData("5 > 5;", 5, ">", 5),
            TestData("5 < 5;", 5, "<", 5),
            TestData("5 == 5;", 5, "==", 5),
            TestData("5 != 5;", 5, "!=", 5),
            TestData("true == true", true, "==", true),
            TestData("true != false", true, "!=", false),
            TestData("false == false", false, "==", false)
        )

        tests.forEach { (input, leftValue, operator, rightValue) ->
            val program = createProgram(input)

            countStatements(1, program)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                testInfixExpression(statement.expression, leftValue, operator, rightValue)
            }
        }
    }

    @Test
    fun `operator precedence`() {
        val tests = listOf(
            "-a * b" to "((-a) * b)",
            "!-a" to "(!(-a))",
            "a + b + c" to "((a + b) + c)",
            "a + b - c" to "((a + b) - c)",
            "a * b * c" to "((a * b) * c)",
            "a * b / c" to "((a * b) / c)",
            "a + b / c" to "(a + (b / c))",
            "a + b * c + d / e - f" to "(((a + (b * c)) + (d / e)) - f)",
            "3 + 4; -5 * 5" to "(3 + 4)((-5) * 5)",
            "5 > 4 == 3 < 4" to "((5 > 4) == (3 < 4))",
            "5 < 4 != 3 > 4" to "((5 < 4) != (3 > 4))",
            "3 + 4 * 5 == 3 * 1 + 4 * 5" to "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
            "true" to "true",
            "false" to "false",
            "3 > 5 == false" to "((3 > 5) == false)",
            "3 < 5 == true" to "((3 < 5) == true)",
            "1 + (2 + 3) + 4" to "((1 + (2 + 3)) + 4)",
            "(5 + 5) * 2" to "((5 + 5) * 2)",
            "2 / (5 + 5)" to "(2 / (5 + 5))",
            "(5 + 5) * 2 * (5 + 5)" to "(((5 + 5) * 2) * (5 + 5))",
            "-(5 + 5)" to "(-(5 + 5))",
            "!(true == true)" to "(!(true == true))",
            "a + add(b * c) + d" to "((a + add((b * c))) + d)",
            "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))" to "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))",
            "add(a + b + c * d / f + g)" to "add((((a + b) + ((c * d) / f)) + g))",
            "a * [1, 2, 3, 4][b * c] * d" to "((a * ([1, 2, 3, 4][(b * c)])) * d)",
            "add(a * b[2], b[1], 2 * [1, 2][1])" to "add((a * (b[2])), (b[1]), (2 * ([1, 2][1])))",

            )

        tests.forEach { (input, expected) ->
            val program = createProgram(input)

            val actual = program.toString()
            assertEquals(actual, expected, "expected ${expected}. got=$actual")
        }
    }

    @Test
    fun `boolean expression`() {
        val tests = listOf(
            "true" to true,
            "false" to false,
        )

        tests.forEach { (input, expectedBoolean) ->
            val program = createProgram(input)
            countStatements(1, program)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { boolean: BooleanLiteral ->

                    assertEquals(
                        boolean.value,
                        expectedBoolean,
                        "value not $expectedBoolean, got=${boolean.value}"
                    )
                }
            }
        }
    }

    @Test
    fun `if expression`() {
        val input = "if (x < y) { x }"
        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { exp: IfExpression ->
                testInfixExpression(exp.condition, "x", "<", "y")

                assertEquals(
                    exp.consequence?.statements?.size,
                    1,
                    "Consequences does not contain 1 statement. got=${exp.consequence?.statements?.size}"
                )

                checkType(exp.consequence?.statements?.first()) { consequence: ExpressionStatement ->
                    testIdentifier(consequence.expression, "x")
                }

                assertNull(exp.alternative, "exp.alternative was not nil got=${exp.alternative}")
            }
        }
    }

    @Test
    fun `if else expression`() {
        val input = "if (x < y) { x } else { y }"
        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { exp: IfExpression ->
                testInfixExpression(exp.condition, "x", "<", "y")

                assertEquals(
                    exp.consequence?.statements?.size,
                    1,
                    "Consequences does not contain 1 statement. got=${exp.consequence?.statements?.size}"
                )

                checkType(exp.consequence?.statements?.first()) { consequence: ExpressionStatement ->
                    testIdentifier(consequence.expression, "x")
                }

                assertEquals(
                    1,
                    exp.alternative?.statements?.size,
                    "alternative does not contain 1 statement, got=${exp.alternative?.statements?.size}"
                )

                checkType(exp.alternative?.statements?.first()) { alternative: ExpressionStatement ->
                    testIdentifier(alternative.expression, "y")
                }
            }
        }
    }

    @Test
    fun `function literal parsing`() {
        val input = "fn(x, y) { x + y;}"

        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { function: FunctionLiteral ->
                testLiteralExpression(function.parameters?.get(0), "x")
                testLiteralExpression(function.parameters?.get(1), "y")

                assertEquals(
                    function.body?.statements?.size,
                    1,
                    "function.body.statements has not 1 statement, got=${function.body?.statements?.size}"
                )

                checkType(function.body?.statements?.first()) { body: ExpressionStatement ->
                    testInfixExpression(body.expression, "x", "+", "y")
                }
            }
        }
    }


    @Test
    fun `function parameter parsing`() {
        val tests = listOf(
            "fn() {}" to emptyList(),
            "fn(x) {}" to listOf("x"),
            "fn(x, y, z) {}" to listOf("x", "y", "z"),
        )

        tests.forEach { (input, expectedParams) ->
            val program = createProgram(input)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { function: FunctionLiteral ->
                    assertEquals(
                        function.parameters?.size,
                        expectedParams.size,
                        "length of parameters is wrong. want ${expectedParams.size}. got=${function.parameters?.size}"
                    )

                    expectedParams.forEachIndexed { i, param ->
                        testLiteralExpression(function.parameters?.get(i), param)
                    }
                }
            }
        }
    }

    @Test
    fun `call expression parsing`() {
        val input = "add(1, 2 * 3, 4+5)"

        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { exp: CallExpression ->
                testIdentifier(exp.function, "add")

                assertEquals(exp.arguments?.size, 3, "wrong length of arguments. got=${exp.arguments?.size}")

                testLiteralExpression(exp.arguments?.get(0), 1)
                testInfixExpression(exp.arguments?.get(1), 2, "*", 3)
                testInfixExpression(exp.arguments?.get(2), 4, "+", 5)

            }
        }
    }

    @Test
    fun `string literal expression`() {
        val input = """"hello world";"""

        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { literal: StringLiteral ->
                assertEquals(literal.value, "hello world")
            }
        }
    }

    @Test
    fun `parsing array literal`() {
        val input = "[1, 2 * 2, 3 + 3]"

        val program = createProgram(input)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { array: ArrayLiteral ->
                testLongLiteral(array.elements!!.first(), 1)
                testInfixExpression(array.elements!![1], 2, "*", 2)
                testInfixExpression(array.elements!![2], 3, "+", 3)
            }

        }
    }

    @Test
    fun `parsing index expression`() {
        val input = "myArray[1 + 1]"

        val program = createProgram(input)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { index: IndexExpression ->
                testIdentifier(index.left, "myArray")
                testInfixExpression(index.index, 1, "+", 1)
            }
        }
    }

    @Test
    fun `hash literal string keys`() {
        val input = """{"one": 1, "two": 2, "three": 3}"""

        val program = createProgram(input)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            checkType(statement.expression) { hash: HashLiteral ->
                assertEquals(3, hash.pairs.size, "hash.pairs has the wrong length")

                val expected = mapOf("one" to 1, "two" to 2, "three" to 3)

                hash.pairs.forEach { (key, value) ->
                    checkType(key) { literal: StringLiteral ->
                        val expectedValue = expected[literal.toString()]
                        testLiteralExpression(value, expectedValue)
                    }
                }

            }
        }
    }

    @Test
    fun `function literal with name`() {
        val input = "let myFunction = fn() {};"
        val program = createProgram(input)
        checkType(program.statements.first()) { statement: LetStatement ->
            checkType(statement.value) { function: FunctionLiteral ->
                assertEquals("myFunction", function.name)
            }
        }
    }

    private fun <T> testInfixExpression(
        expression: Expression?,
        leftValue: T,
        operator: String,
        rightValue: T
    ) {
        checkType(expression) { exp: InfixExpression ->
            testLiteralExpression(exp.left, leftValue)
            assertEquals(operator, exp.operator, "exp.operator is not $operator. got=${exp.operator}")
            testLiteralExpression(exp.right, rightValue)
        }
    }

    private fun <T> testLiteralExpression(value: Expression?, expectedValue: T) {
        when (expectedValue) {
            is Long -> testLongLiteral(value, expectedValue)
            is Int -> testLongLiteral(value, expectedValue.toLong())
            is String -> testIdentifier(value, expectedValue)
            is Boolean -> testBooleanLiteral(value, expectedValue)
            else -> fail("type of value not handled. got=${expectedValue!!::class.simpleName}")
        }
    }

    private fun testBooleanLiteral(expression: Expression?, b: Boolean) =
        checkType(expression) { exp: BooleanLiteral ->
            assertEquals(b, exp.value, "exp.value not $b. got=${exp.value}")
            assertEquals(b.toString(), exp.tokenLiteral(), "exp.tokenLiteral() not $b. got=${exp.tokenLiteral()}")
        }

    private fun testIdentifier(expression: Expression?, string: String) = checkType(expression) { exp: Identifier ->
        assertEquals(string, exp.value, "exp.value no $string. got=${exp.value}")
        assertEquals(string, exp.tokenLiteral(), "exp.tokenLiteral() no $string. got=${exp.tokenLiteral()}")
    }

    private fun testLongLiteral(expression: Expression?, l: Long) = checkType(expression) { exp: IntegerLiteral ->
        assertEquals(l, exp.value, "exp.value not $l. got=${exp.value}")
        assertEquals(l.toString(), exp.tokenLiteral(), "exp.tokenLiteral() not $l. got=${exp.tokenLiteral()}")
    }

    private fun testLetStatement(statement: Statement, expectedIdentifier: String) {
        assertEquals(
            "let",
            statement.tokenLiteral(),
            "statement.tokenLiteral() not 'let'. got=${statement.tokenLiteral()}"
        )

        checkType(statement) { letStatement: LetStatement ->
            assertEquals(
                expectedIdentifier,
                letStatement.name.value,
                "letStatement.name.value not $expectedIdentifier. got=${letStatement.name.value}"
            )
            assertEquals(
                expectedIdentifier,
                letStatement.name.tokenLiteral(),
                "letStatement.name.tokenLiteral() not $expectedIdentifier. got=${letStatement.name.tokenLiteral()}"
            )
        }
    }

    private fun countStatements(i: Int, program: Program) {
        val size = program.statements.size
        assertEquals(i, size, "wrong length of arguments. got=$size")
    }

    private fun createProgram(input: String): Program {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        val program = parser.parseProgram()
        checkParserErrors(parser)
        return program
    }

    private fun checkParserErrors(parser: Parser) {
        val errors = parser.errors()
        if (errors.isNotEmpty()) {
            fail("parser has ${errors.size} errors: \n${errors.joinToString(" \n")}")
        }


    }
}