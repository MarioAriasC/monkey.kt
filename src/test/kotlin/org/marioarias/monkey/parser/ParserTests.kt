package org.marioarias.monkey.parser

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.checkType
import org.marioarias.monkey.isType
import org.marioarias.monkey.lexer.Lexer
import org.testng.Assert
import org.testng.annotations.Test


class ParserTests {

    @Test
    fun `let statements`() {
        data class TestData<T>(val input: String, val expectedIdentifier: String, val expectedValue: T)

        val tests = listOf(
            TestData("let x = 5;", "x", 5),
            TestData("let y = true;", "y", true),
            TestData("let foobar = y;", "foobar", "y")
        )

        tests.forEach { (input, expectedIdentifier, expectedValue) ->
            val program = createProgram(input)

            countStatements(1, program)

            val statement = program.statements.first()

            if (!testLetStatement(statement, expectedIdentifier)) {
                return
            }

            val value = (statement as LetStatement).value
            if (!testLiteralExpression(value, expectedValue)) {
                return
            }
        }
    }

    @Test
    fun `return statements`() {
        data class TestData<T>(val input: String, val expectedValue: T)

        val tests = listOf(
            TestData("return 5;", 5),
            TestData("return true;", true),
            TestData("return foobar;", "foobar")
        )

        tests.forEach { (input, expectedValue) ->
            val program = createProgram(input)

            countStatements(1, program)

            checkType(program.statements.first()) { statement: ReturnStatement ->
                if (statement.tokenLiteral() != "return") {
                    Assert.fail("statement.tokenLiteral() not 'return', got=${statement.tokenLiteral()}")
                }
                if (testLiteralExpression(statement.returnValue, expectedValue)) {
                    return
                }
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
                when {
                    identifier.value != "foobar" -> Assert.fail("identifier.value not 'foobar'. got=${identifier.value}")
                    identifier.tokenLiteral() != "foobar" -> Assert.fail("identifier.tokenLiteral() not 'foobar'. got=${identifier.tokenLiteral()}")
                }
            }

        }
    }

    @Test
    fun `integer literals`() {
        val input = "5;"

        val program = createProgram(input)

        countStatements(1, program)

        checkType(program.statements.first()) { statement: ExpressionStatement ->
            when (val identifier = statement.expression) {
                is IntegerLiteral -> {
                    when {
                        identifier.value != 5.toLong() -> Assert.fail("identifier.value not 5. got=${identifier.value}")
                        identifier.tokenLiteral() != "5" -> Assert.fail("identifier.tokenLiteral() not '5'. got=${identifier.tokenLiteral()}")
                    }
                }
                else -> {
                    Assert.fail("statement.expression not Identifier. got=${identifier!!::class.java}")
                }
            }
        }
    }

    @Test
    fun `parsing prefix expressions`() {
        data class TestData<T>(val input: String, val operator: String, val value: T)

        val tests = listOf(
            TestData("!5;", "!", 5),
            TestData("-15;", "-", 15),
            TestData("!true;", "!", true),
            TestData("!false;", "!", false),
        )

        tests.forEach { (input, operator, value) ->
            val program = createProgram(input)

            countStatements(1, program)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { expression: PrefixExpression ->
                    Assert.assertEquals(
                        expression.operator,
                        operator,
                        "expression.operator is not ${operator}. got=${expression.operator}"
                    )
                    if (!testLiteralExpression(expression.right, value)) {
                        return@forEach
                    }
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
        data class TestData(val input: String, val expected: String)

        val tests = listOf(
            TestData(
                "-a * b",
                "((-a) * b)",
            ),
            TestData(
                "!-a",
                "(!(-a))",
            ),
            TestData(
                "a + b + c",
                "((a + b) + c)",
            ),
            TestData(
                "a + b - c",
                "((a + b) - c)",
            ),
            TestData(
                "a * b * c",
                "((a * b) * c)",
            ),
            TestData(
                "a * b / c",
                "((a * b) / c)",
            ),
            TestData(
                "a + b / c",
                "(a + (b / c))",
            ),
            TestData(
                "a + b * c + d / e - f",
                "(((a + (b * c)) + (d / e)) - f)",
            ),
            TestData(
                "3 + 4; -5 * 5",
                "(3 + 4)((-5) * 5)",
            ),
            TestData(
                "5 > 4 == 3 < 4",
                "((5 > 4) == (3 < 4))",
            ),
            TestData(
                "5 < 4 != 3 > 4",
                "((5 < 4) != (3 > 4))",
            ),
            TestData(
                "3 + 4 * 5 == 3 * 1 + 4 * 5",
                "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
            ),
            TestData(
                "true",
                "true",
            ),
            TestData(
                "false",
                "false",
            ),
            TestData(
                "3 > 5 == false",
                "((3 > 5) == false)",
            ),
            TestData(
                "3 < 5 == true",
                "((3 < 5) == true)",
            ),
            TestData(
                "1 + (2 + 3) + 4",
                "((1 + (2 + 3)) + 4)",
            ),
            TestData(
                "(5 + 5) * 2",
                "((5 + 5) * 2)",
            ),
            TestData(
                "2 / (5 + 5)",
                "(2 / (5 + 5))",
            ),
            TestData(
                "(5 + 5) * 2 * (5 + 5)",
                "(((5 + 5) * 2) * (5 + 5))",
            ),
            TestData(
                "-(5 + 5)",
                "(-(5 + 5))",
            ),
            TestData(
                "!(true == true)",
                "(!(true == true))",
            ),
            TestData(
                "a + add(b * c) + d",
                "((a + add((b * c))) + d)",
            ),
            TestData(
                "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))",
                "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))",
            ),
            TestData(
                "add(a + b + c * d / f + g)",
                "add((((a + b) + ((c * d) / f)) + g))",
            )
        )

        tests.forEach { (input, expected) ->
            val program = createProgram(input)

            val actual = program.toString()
            Assert.assertEquals(actual, expected, "expected ${expected}. got=$actual")
        }
    }

    @Test
    fun `boolean expression`() {
        data class TestData(val input: String, val expectedBoolean: Boolean)

        val tests = listOf(
            TestData("true", true),
            TestData("false", false),
        )

        tests.forEach { (input, expectedBoolean) ->
            val program = createProgram(input)
            countStatements(1, program)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { boolean: BooleanLiteral ->

                    Assert.assertEquals(
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
                if (!testInfixExpression(exp.condition, "x", "<", "y")) {
                    return
                }

                Assert.assertEquals(
                    exp.consequence?.statements?.size,
                    1,
                    "Consequences does not contain 1 statement. got=${exp.consequence?.statements?.size}"
                )

                checkType(exp.consequence?.statements?.first()) { consequence: ExpressionStatement ->
                    if (!testIdentifier(consequence.expression, "x")) {
                        return
                    }
                }

                Assert.assertNull(exp.alternative, "exp.alternative was not nil got=${exp.alternative}")
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
                if (!testInfixExpression(exp.condition, "x", "<", "y")) {
                    return
                }

                Assert.assertEquals(
                    exp.consequence?.statements?.size,
                    1,
                    "Consequences does not contain 1 statement. got=${exp.consequence?.statements?.size}"
                )

                checkType(exp.consequence?.statements?.first()) { consequence: ExpressionStatement ->
                    if (!testIdentifier(consequence.expression, "x")) {
                        return
                    }
                }

                Assert.assertEquals(
                    1,
                    exp.alternative?.statements?.size,
                    "alternative does not contain 1 statement, got=${exp.alternative?.statements?.size}"
                )

                checkType(exp.alternative?.statements?.first()) { alternative: ExpressionStatement ->
                    if (!testIdentifier(alternative.expression, "y")) {
                        return
                    }
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

                Assert.assertEquals(
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
        data class TestData(val input: String, val expectedParams: List<String>)

        val tests = listOf(
            TestData("fn() {}", emptyList()),
            TestData("fn(x) {}", listOf("x")),
            TestData("fn(x, y, z) {}", listOf("x", "y", "z")),
        )

        tests.forEach { (input, expectedParams) ->
            val program = createProgram(input)

            checkType(program.statements.first()) { statement: ExpressionStatement ->
                checkType(statement.expression) { function: FunctionLiteral ->
                    Assert.assertEquals(
                        function.parameters?.size,
                        expectedParams.size,
                        "length of parameters is wrong. want ${expectedParams.size}. got=${function.parameters?.size}"
                    )

                    expectedParams.forEachIndexed() { i, param ->
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
                if (!testIdentifier(exp.function, "add")) {
                    return
                }



                Assert.assertEquals(exp.arguments?.size, 3, "wrong length of arguments. got=${exp.arguments?.size}")

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
                Assert.assertEquals(literal.value, "hello world")
            }
        }
    }


    private fun <L, R> testInfixExpression(
        expression: Expression?,
        leftValue: L,
        operator: String,
        rightValue: R
    ): Boolean {
        return isType(expression) { exp: InfixExpression ->
            when {
                !testLiteralExpression(exp.left, leftValue) -> false
                exp.operator != operator -> {
                    Assert.fail("exp.operator is not $operator. got=${exp.operator}")
                    false
                }
                !testLiteralExpression(exp.right, rightValue) -> false
                else -> true
            }
        }
    }

    private fun <T> testLiteralExpression(value: Expression?, expectedValue: T): Boolean {
        return when (expectedValue) {
            is Long -> testLongLiteral(value, expectedValue)
            is Int -> testLongLiteral(value, expectedValue.toLong())
            is String -> testIdentifier(value, expectedValue)
            is Boolean -> testBooleanLiteral(value, expectedValue)
            else -> {
                Assert.fail("type of value not handled. got=${expectedValue!!::class.java}")
                false
            }
        }
    }

    private fun testBooleanLiteral(expression: Expression?, b: Boolean): Boolean {


        return isType(expression) { exp: BooleanLiteral ->
            when {
                exp.value != b -> {
                    Assert.fail("exp.value not $b. got=${exp.value}")
                    false
                }
                exp.tokenLiteral() != b.toString() -> {
                    Assert.fail("exp.tokenLiteral() not $b. got=${exp.tokenLiteral()}")
                    false
                }
                else -> true
            }
        }

    }

    private fun testIdentifier(expression: Expression?, string: String): Boolean {

        return isType(expression) { exp: Identifier ->
            when {
                exp.value != string -> {
                    Assert.fail("exp.value no $string. got=${exp.value}")
                    false
                }
                exp.tokenLiteral() != string -> {
                    Assert.fail("exp.tokenLiteral() no $string. got=${exp.tokenLiteral()}")
                    false
                }
                else -> true
            }
        }

    }

    private fun testLongLiteral(expression: Expression?, l: Long): Boolean {

        return isType(expression) { exp: IntegerLiteral ->
            when {
                exp.value != l -> {
                    Assert.fail("exp.value not $l. got=${exp.value}")
                    false
                }
                exp.tokenLiteral() != l.toString() -> {
                    Assert.fail("exp.tokenLiteral() not $l. got=${exp.tokenLiteral()}")
                    false
                }
                else -> true
            }
        }

    }

    private fun testLetStatement(statement: Statement, expectedIdentifier: String): Boolean {
        if (statement.tokenLiteral() != "let") {
            Assert.fail("statement.tokenLiteral() not 'let'. got=${statement.tokenLiteral()}")
            return false
        }

        return isType(statement) { letStatement: LetStatement ->
            when {
                letStatement.name.value != expectedIdentifier -> {
                    Assert.fail("letStatement.name.value not $expectedIdentifier. got=${letStatement.name.value}")
                    false
                }
                letStatement.name.tokenLiteral() != expectedIdentifier -> {
                    Assert.fail("letStatement.name.tokenLiteral() not $expectedIdentifier. got=${letStatement.name.tokenLiteral()}")
                    false
                }
                else -> true
            }
        }

    }

    private fun countStatements(i: Int, program: Program) {
        val size = program.statements.size
        Assert.assertEquals(i, size, "wrong length of arguments. got=$size")
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
        if (errors.isEmpty()) {
            return
        }
        Assert.fail("parser has ${errors.size} errors: \n${errors.joinToString(" \n")}")

    }
}