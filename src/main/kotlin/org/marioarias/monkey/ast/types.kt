package org.marioarias.monkey.ast

import org.marioarias.monkey.token.Token

interface Node {
    fun tokenLiteral(): String
    override fun toString(): String
}

interface Statement : Node
interface Expression : Node

class Program(val statements: List<Statement>) : Node {
    override fun tokenLiteral(): String {
        return if (statements.isEmpty()) "" else statements.first().tokenLiteral()
    }

    override fun toString(): String {
        return statements.joinToString(separator = "")
    }
}

interface ExpressionWithToken : Expression {
    val token: Token

    override fun tokenLiteral(): String = token.literal
}

interface StatementWithToken : Statement {
    val token: Token
    override fun tokenLiteral(): String = token.literal
}


class Identifier(override val token: Token, val value: String) : ExpressionWithToken {
    override fun toString(): String {
        return value
    }
}

class LetStatement(override val token: Token, val name: Identifier, val value: Expression?) : StatementWithToken {
    override fun toString(): String {

        return "${tokenLiteral()} $name = ${value?.toString() ?: ""};"
    }
}


abstract class LiteralExpression<T>(override val token: Token, val value: T) : ExpressionWithToken {
    override fun toString(): String {
        return token.literal
    }
}

class IntegerLiteral(token: Token, value: Long) : LiteralExpression<Long>(token, value)
class BooleanLiteral(token: Token, value: Boolean) : LiteralExpression<Boolean>(token, value)

class ReturnStatement(override val token: Token, val returnValue: Expression?) : StatementWithToken {
    override fun toString(): String {
        return "${tokenLiteral()} ${returnValue?.toString() ?: ""};"

    }
}

class ExpressionStatement(override val token: Token, val expression: Expression?) : StatementWithToken {
    override fun toString(): String {
        return expression?.toString() ?: ""
    }
}

class PrefixExpression(override val token: Token, val operator: String, val right: Expression?) : ExpressionWithToken {
    override fun toString(): String {
        return "($operator$right)"
    }
}

class InfixExpression(override val token: Token, val left: Expression?, val operator: String, val right: Expression?) :
    ExpressionWithToken {
    override fun toString(): String {
        return "($left $operator $right)"
    }
}

class CallExpression(override val token: Token, val function: Expression?, val arguments: List<Expression?>?) :
    ExpressionWithToken {
    override fun toString(): String {
        return "${function?.toString()}(${arguments?.joinToString()})"
    }
}


class BlockStatement(override val token: Token, val statements: List<Statement?>?) : StatementWithToken {
    override fun toString(): String {
        return statements?.joinToString(separator = "") ?: ""
    }
}

class IfExpression(
    override val token: Token,
    val condition: Expression?,
    val consequence: BlockStatement?,
    val alternative: BlockStatement?
) : ExpressionWithToken {
    override fun toString(): String {
        return "if$condition $consequence ${if (alternative != null) "else $alternative" else ""}"
    }
}

class FunctionLiteral(override val token: Token, val parameters: List<Identifier>?, val body: BlockStatement?) :
    ExpressionWithToken {
    override fun toString(): String {
        return "${tokenLiteral()}(${parameters?.joinToString()}) $body"
    }
}

class StringLiteral(override val token: Token, val value: String) : ExpressionWithToken {
    override fun toString(): String {
        return value
    }
}

class ArrayLiteral(override val token: Token, val elements: List<Expression?>?) : ExpressionWithToken {
    override fun toString(): String {
        return "[${elements?.joinToString(separator = ", ")}]"
    }
}

class IndexExpression(override val token: Token, val left: Expression?, val index: Expression?) : ExpressionWithToken {
    override fun toString(): String {
        return "($left[$index])"
    }
}