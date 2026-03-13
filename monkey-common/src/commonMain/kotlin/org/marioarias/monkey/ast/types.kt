package org.marioarias.monkey.ast

import org.marioarias.monkey.token.Token

interface Node {
    fun tokenLiteral(): String
    override fun toString(): String
}

interface Statement : Node {
    val token: Token

    override fun tokenLiteral(): String = token.literal
}

typealias Expression = Statement

class Program(val statements: List<Statement>)  {
    override fun toString(): String {
        return statements.joinToString(separator = "")
    }
}

class LetStatement(override val token: Token, val name: Identifier, val value: Expression?) :
    Statement {
    override fun toString(): String = "${tokenLiteral()} $name = ${value?.toString() ?: ""};"
}


abstract class LiteralExpression<T>(override val token: Token, val value: T) : Expression {
    override fun toString(): String {
        return token.literal
    }
}

class IntegerLiteral(token: Token, value: Long) : LiteralExpression<Long>(token, value)
class BooleanLiteral(token: Token, value: Boolean) : LiteralExpression<Boolean>(token, value)

abstract class StringValue(override val token: Token, val value: String) : Expression {
    override fun toString(): String = value
}

class StringLiteral(token: Token, value: String) : StringValue(token, value)
class Identifier(token: Token, value: String) : StringValue(token, value)

class ReturnStatement(override val token: Token, val returnValue: Expression?) : Statement {
    override fun toString(): String = "${tokenLiteral()} ${returnValue?.toString() ?: ""};"
}

class ExpressionStatement(override val token: Token, val expression: Expression?) : Statement {
    override fun toString(): String {
        return expression?.toString() ?: ""
    }
}

class PrefixExpression(override val token: Token, val operator: String, val right: Expression?) :
    Expression {
    override fun toString(): String = "($operator$right)"
}

class InfixExpression(override val token: Token, val left: Expression?, val operator: String, val right: Expression?) :
    Expression {
    override fun toString(): String {
        return "($left $operator $right)"
    }
}

class CallExpression(override val token: Token, val function: Expression?, val arguments: List<Expression?>?) :
    Expression {
    override fun toString(): String {
        return "${function?.toString()}(${arguments?.joinToString()})"
    }
}


class BlockStatement(override val token: Token, val statements: List<Statement?>?) : Statement {
    override fun toString(): String {
        return statements?.joinToString(separator = "") ?: ""
    }
}

class IfExpression(
    override val token: Token,
    val condition: Expression?,
    val consequence: BlockStatement?,
    val alternative: BlockStatement?
) : Expression {
    override fun toString(): String {
        return "if $condition $consequence ${if (alternative != null) "else $alternative" else ""}"
    }
}

class FunctionLiteral(
    override val token: Token,
    val parameters: List<Identifier>?,
    val body: BlockStatement?,
    var name: String = ""
) :
    Expression {
    override fun toString(): String {
        return "${tokenLiteral()}${if (name.isNotEmpty()) "<$name>" else ""}(${parameters?.joinToString()}) $body"
    }
}


class ArrayLiteral(override val token: Token, val elements: List<Expression?>?) : Expression {
    override fun toString(): String = "[${elements?.joinToString()}]"
}

class IndexExpression(override val token: Token, val left: Expression?, val index: Expression?) :
    Expression {
    override fun toString(): String {
        return "($left[$index])"
    }
}

class HashLiteral(override val token: Token, val pairs: Map<Expression, Expression>) :
    Expression {
    override fun toString(): String {
        return "{${pairs.keys.joinToString { key -> "$key:${pairs[key]}" }}}"
    }
}