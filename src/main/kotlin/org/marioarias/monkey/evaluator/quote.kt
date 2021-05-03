package org.marioarias.monkey.evaluator

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.evaluator.Evaluator.eval
import org.marioarias.monkey.objects.MBoolean
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.MQuote
import org.marioarias.monkey.token.Token
import org.marioarias.monkey.token.TokenType

fun quote(node: Node, env: Environment): MObject {
    return MQuote(evalUnquoteCalls(node, env))
}

fun evalUnquoteCalls(quote: Node, env: Environment): Node? {
    return modify(quote) { node ->
        if (!node.isUnquoteCall()) {
            node
        } else {
            val call = node as CallExpression
            if (call.arguments?.size != 1) {
                node
            } else {
                val unquoted = eval(call.arguments.first(), env)
                unquoted.toASTNode()
            }
        }
    }
}

private fun MObject?.toASTNode(): Node? {
    return when (this) {
        is MInteger -> {
            IntegerLiteral(Token(TokenType.INT, this.value.toString()), this.value)
        }
        is MBoolean -> {
            val token = if (this.value) {
                Token(TokenType.TRUE, "true")
            } else {
                Token(TokenType.FALSE, "false")
            }
            BooleanLiteral(token, this.value)
        }
        is MQuote -> {
            this.node
        }
        else -> null
    }
}

fun Node.isUnquoteCall(): Boolean {
    return when (this) {
        is CallExpression -> {
            this.function?.tokenLiteral() == "unquote"
        }
        else -> false
    }
}
