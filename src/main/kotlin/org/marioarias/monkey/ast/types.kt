package org.marioarias.monkey.ast

import org.marioarias.monkey.token.Token

interface Node {
    fun tokenLiteral(): String
    override fun toString(): String
}


abstract class NodeAdapter {
    override fun equals(other: Any?): Boolean {
        return if (other != null) {
            other.toString() == toString()
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }
}

interface Statement : Node
interface Expression : Node

class Program(val statements: List<Statement>) : NodeAdapter(), Node {
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


class Identifier(override val token: Token, val value: String) : NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return value
    }
}

class LetStatement(override val token: Token, val name: Identifier, val value: Expression?) : NodeAdapter(),
    StatementWithToken {
    override fun toString(): String {

        return "${tokenLiteral()} $name = ${value?.toString() ?: ""};"
    }
}


abstract class LiteralExpression<T>(override val token: Token, val value: T) : NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return token.literal
    }
}

class IntegerLiteral(token: Token, value: Long) : LiteralExpression<Long>(token, value)
class BooleanLiteral(token: Token, value: Boolean) : LiteralExpression<Boolean>(token, value)

class ReturnStatement(override val token: Token, val returnValue: Expression?) : NodeAdapter(), StatementWithToken {
    override fun toString(): String {
        return "${tokenLiteral()} ${returnValue?.toString() ?: ""};"

    }
}

class ExpressionStatement(override val token: Token, val expression: Expression?) : NodeAdapter(), StatementWithToken {
    override fun toString(): String {
        return expression?.toString() ?: ""
    }
}

class PrefixExpression(override val token: Token, val operator: String, val right: Expression?) : NodeAdapter(),
    ExpressionWithToken {
    override fun toString(): String {
        return "($operator$right)"
    }
}

class InfixExpression(override val token: Token, val left: Expression?, val operator: String, val right: Expression?) :
    NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "($left $operator $right)"
    }
}

class CallExpression(override val token: Token, val function: Expression?, val arguments: List<Expression?>?) :
    NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "${function?.toString()}(${arguments?.joinToString()})"
    }
}


class BlockStatement(override val token: Token, val statements: List<Statement?>?) : NodeAdapter(), StatementWithToken {
    override fun toString(): String {
        return statements?.joinToString(separator = "") ?: ""
    }
}

class IfExpression(
    override val token: Token,
    val condition: Expression?,
    val consequence: BlockStatement?,
    val alternative: BlockStatement?
) : NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "if$condition $consequence ${if (alternative != null) "else $alternative" else ""}"
    }
}

class FunctionLiteral(override val token: Token, val parameters: List<Identifier>?, val body: BlockStatement?) :
    NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "${tokenLiteral()}(${parameters?.joinToString()}) $body"
    }
}

class StringLiteral(override val token: Token, val value: String) : NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return value
    }
}

class ArrayLiteral(override val token: Token, val elements: List<Expression?>?) : NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "[${elements?.joinToString(separator = ", ")}]"
    }
}

class IndexExpression(override val token: Token, val left: Expression?, val index: Expression?) : NodeAdapter(),
    ExpressionWithToken {
    override fun toString(): String {
        return "($left[$index])"
    }
}

class HashLiteral(override val token: Token, val pairs: Map<Expression, Expression>) : NodeAdapter(),
    ExpressionWithToken {
    override fun toString(): String {
        return "{${pairs.keys.joinToString { key -> "$key:${pairs[key]}" }}}"
    }
}

class MacroLiteral(override val token: Token, val parameters: List<Identifier>?, val body: BlockStatement) :
    NodeAdapter(), ExpressionWithToken {
    override fun toString(): String {
        return "${token.literal} (${parameters?.joinToString()}) $body"
    }
}