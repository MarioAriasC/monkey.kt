package org.marioarias.monkey.evaluator

import org.marioarias.monkey.checkType
import org.marioarias.monkey.evaluator.EvaluatorTests.Companion.testEval
import org.marioarias.monkey.objects.MQuote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class QuoteTests {
    data class TestData(val input: String, val expected: String)

    @Test
    fun quote() {
        listOf(
            TestData(
                "quote(5)",
                "5"
            ),
            TestData(
                "quote(5 + 8)",
                "(5 + 8)",
            ),
            TestData(
                "quote(foobar)",
                "foobar",
            ),
            TestData(
                "quote(foobar + barfoo)",
                "(foobar + barfoo)",
            ),
        ).forEach { (input, expected) ->
            testQuote(input, expected)
        }
    }

    @Test
    fun `quote unquote`() {
        listOf(
            TestData(
                "quote(unquote(4))",
                "4",
            ),
            TestData(
                "quote(unquote(4 + 4))",
                "8",
            ),
            TestData(
                "quote(8 + unquote(4 + 4))",
                "(8 + 8)",
            ),
            TestData(
                "quote(unquote(4 + 4) + 8)",
                "(8 + 8)",
            ),
            TestData(
                """let foobar = 8;
          					quote(foobar)""",
                "foobar",
            ),
            TestData(
                """let foobar = 8;
          					quote(unquote(foobar))""",
                "8",
            ),
            TestData(
                "quote(unquote(true))",
                "true",
            ),
            TestData(
                "quote(unquote(true == false))",
                "false",
            ),
            TestData(
                "quote(unquote(quote(4 + 4)))",
                "(4 + 4)",
            ),
            TestData(
                """let quotedInfixExpression = quote(4 + 4);
          					quote(unquote(4 + 4) + unquote(quotedInfixExpression))""",
                "(8 + (4 + 4))",
            ),
        )
    }

    private fun testQuote(input: String, expected: String) {
        val evaluated = testEval(input)
        checkType(evaluated) { quote: MQuote ->
            assertNotNull(quote.node)
            assertEquals(quote.node?.toString(), expected)
        }
    }
}