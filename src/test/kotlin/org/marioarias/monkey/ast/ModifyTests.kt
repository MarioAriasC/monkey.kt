package org.marioarias.monkey.ast

import org.marioarias.monkey.token.Token
import org.marioarias.monkey.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals


class ModifyTests {
    @Test
    fun modify() {
        val token = Token(TokenType.INT, "1")
        fun one() = IntegerLiteral(token, 1)
        fun two() = IntegerLiteral(token, 2)

        fun statements(intLiteral: IntegerLiteral) = listOf(ExpressionStatement(token, intLiteral))

        val turnOneIntoTwo = { node: Node ->
            when (node) {
                is IntegerLiteral -> {
                    when (node.value) {
                        1L -> two()
                        else -> node
                    }
                }
                else -> node
            }
        }

        data class TestData(val input: Node, val expected: Node)

        listOf(
            TestData(one(), two()),
            TestData(
                Program(statements(one())),
                Program(statements(two()))
            ),
            TestData(
                InfixExpression(token, left = one(), "+", right = two()),
                InfixExpression(token, left = two(), "+", right = two())
            ),
            TestData(
                InfixExpression(token, left = two(), "+", right = one()),
                InfixExpression(token, left = two(), "+", right = two())
            ),
            TestData(
                PrefixExpression(token, "-", one()),
                PrefixExpression(token, "-", two()),
            ),
            TestData(
                IndexExpression(token, one(), one()),
                IndexExpression(token, two(), two()),
            ),
            TestData(
                IfExpression(
                    token,
                    condition = one(),
                    consequence = BlockStatement(token, statements(one())),
                    alternative = BlockStatement(token, statements(one()))
                ),
                IfExpression(
                    token,
                    condition = two(),
                    consequence = BlockStatement(token, statements(two())),
                    alternative = BlockStatement(token, statements(two()))
                )
            ),
            TestData(
                ReturnStatement(token, one()),
                ReturnStatement(token, two())
            ),
            TestData(
                LetStatement(token, name = Identifier(token, "x"), value = one()),
                LetStatement(token, name = Identifier(token, "x"), value = two())
            ),
            TestData(
                FunctionLiteral(token, parameters = emptyList(), body = BlockStatement(token, statements(one()))),
                FunctionLiteral(token, parameters = emptyList(), body = BlockStatement(token, statements(two()))),
            ),
            TestData(
                ArrayLiteral(token, listOf(one(), one())),
                ArrayLiteral(token, listOf(two(), two())),
            )
        ).forEach { (input, expected) ->
            val modified = modify(input, turnOneIntoTwo)
            assertEquals(expected, modified)
        }

        val hashLiteral = modify(
            HashLiteral(
                token, mapOf(
                    one() to one(),
                    one() to one()
                )
            ), turnOneIntoTwo
        ) as HashLiteral

        hashLiteral.pairs.forEach { (key, value) ->
            when (key) {
                is IntegerLiteral -> assertEquals(2, key.value)
            }
            when (value) {
                is IntegerLiteral -> assertEquals(2, value.value)
            }
        }


    }
}