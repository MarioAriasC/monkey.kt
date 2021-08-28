package org.marioarias.monkey.evaluator

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.objects.*

object Evaluator {

    val NULL = MNull
    val TRUE = MBoolean(true)
    val FALSE = MBoolean(false)

    private fun Boolean.toMonkey(): MBoolean = if (this) TRUE else FALSE

    fun eval(node: Node?, env: Environment): MObject? {
        return when (node) {
            is Program -> evalProgram(node.statements, env)
            is ExpressionStatement -> eval(node.expression, env)
            is IntegerLiteral -> MInteger(node.value)
            is PrefixExpression -> {
                eval(node.right, env).ifNotError { right ->
                    evalPrefixExpression(node.operator, right)
                }

            }
            is InfixExpression -> {
                eval(node.left, env).ifNotError { left ->
                    eval(node.right, env).ifNotError { right ->
                        evalInfixExpression(node.operator, left, right)
                    }
                }
            }
            is BooleanLiteral -> node.value.toMonkey()
            is IfExpression -> evalIfExpression(node, env)
            is BlockStatement -> evalBlockStatement(node, env)
            is ReturnStatement -> {
                eval(node.returnValue, env).ifNotError { value ->
                    MReturnValue(value)
                }
            }
            is LetStatement -> {
                eval(node.value, env).ifNotError { value ->
                    env.put(node.name.value, value)
                }

            }
            is Identifier -> evalIdentifier(node, env)
            is FunctionLiteral -> MFunction(node.parameters, node.body, env)
            is CallExpression -> {
                if (node.function?.tokenLiteral() == "quote") {
                    quote(node.arguments?.first()!!, env)
                } else {
                    eval(node.function, env).ifNotError { function ->
                        val args = evalExpressions(node.arguments, env)
                        if (args.size == 1 && args.first().isError()) {
                            args.first()
                        } else {
                            applyFunction(function, args)
                        }
                    }
                }
            }
            is StringLiteral -> MString(node.value)
            is ArrayLiteral -> {
                val elements = evalExpressions(node.elements, env)
                if (elements.size == 1 && elements.first().isError()) {
                    elements.first()
                } else {
                    MArray(elements)
                }
            }
            is IndexExpression -> {
                val left = eval(node.left, env)

                if (left.isError()) {
                    return left
                }

                val index = eval(node.index, env)

                if (index.isError()) {
                    return index
                }

                return evalIndexExpression(left, index)
            }
            is HashLiteral -> evalHashLiteral(node, env)
            else -> null
        }
    }

    private fun evalHashLiteral(node: HashLiteral, env: Environment): MObject? {

        val pairs = mutableMapOf<HashKey, HashPair>()

        node.pairs.forEach { (keyNode, valueNode) ->
            val key = eval(keyNode, env)
            if (key.isError()) {
                return key
            }
            when (key) {
                is Hashable<*> -> {
                    val value = eval(valueNode, env)
                    if (value.isError()) {
                        return value
                    }
                    pairs[key.hashKey()] = HashPair(key, value!!)
                }
                else -> return MError("unusable as hash key: ${key.typeDesc()}")
            }
        }
        return MHash(pairs)
    }

    private fun evalIndexExpression(left: MObject?, index: MObject?): MObject? {
        return when {
            left is MArray && index is MInteger -> evalArrayIndexExpression(
                left,
                index
            )
            left is MHash -> evalHashIndexExpression(left, index!!)
            else -> MError("index operator not supported: ${left.typeDesc()}")
        }
    }

    private fun evalHashIndexExpression(hash: MHash, index: MObject): MObject {
        return when (index) {
            is Hashable<*> -> {
                val pair = hash.pairs[index.hashKey()]
                pair?.value ?: NULL
            }
            else -> MError("unusable as a hash key: ${index.typeDesc()}")
        }
    }

    private fun evalArrayIndexExpression(array: MArray, index: MInteger): MObject? {
        val mArray = array.elements
        val i = index.value
        val max = mArray.size - 1

        if (i < 0 || i > max) {
            return NULL
        }

        return mArray[i.toInt()]

    }

    private fun applyFunction(function: MObject, args: List<MObject?>): MObject? {
        return when (function) {
            is MFunction -> {
                val extendEnv = extendFunctionEnv(function, args)
                val evaluated = eval(function.body, extendEnv)
                unwrapReturnValue(evaluated)
            }
            is MBuiltinFunction -> function.fn(args)
            else -> MError("not a function: ${function.typeDesc()}")
        }
    }

    private fun unwrapReturnValue(obj: MObject?): MObject? {
        return when (obj) {
            is MReturnValue -> obj.value
            else -> obj
        }
    }

    private fun extendFunctionEnv(function: MFunction, args: List<MObject?>): Environment {
        val env = Environment.newEnclosedEnvironment(function.env)
        function.parameters?.forEachIndexed { i, identifier ->
            env[identifier.value] = args[i]!!
        }
        return env
    }

    private fun MObject?.isError() = if (this != null) {
        this is MError
    } else {
        false
    }

    private fun evalExpressions(arguments: List<Expression?>?, env: Environment): List<MObject?> {
        return arguments!!.map { argument ->
            val evaluated = eval(argument, env)
            if (evaluated.isError()) {
                return listOf(evaluated)
            }
            evaluated
        }

    }

    private fun evalIdentifier(node: Identifier, env: Environment): MObject {
        return when (val value = env[node.value]) {
            null -> {
                when (val builtin = builtins[node.value]) {
                    null -> MError("identifier not found: ${node.value}")
                    else -> builtin
                }
            }
            else -> value
        }
    }

    private fun evalBlockStatement(node: BlockStatement, env: Environment): MObject? {
        var result: MObject? = null

        node.statements?.forEach { statement ->
            result = eval(statement, env)

            if (result != null) {
                val type = (result as MObject)
                if (type is MReturnValue || type is MError) {
                    return result
                }
            }
        }
        return result
    }

    private fun evalIfExpression(ifExpression: IfExpression, env: Environment): MObject? {

        fun isTruthy(obj: MObject?): Boolean {
            return when (obj) {
                NULL -> false
                TRUE -> true
                FALSE -> false
                else -> true
            }
        }

        return eval(ifExpression.condition, env).ifNotError { condition ->
            when {
                isTruthy(condition) -> eval(ifExpression.consequence, env)
                ifExpression.alternative != null -> eval(ifExpression.alternative, env)
                else -> NULL
            }
        }
    }


    private fun evalInfixExpression(operator: String, left: MObject, right: MObject): MObject {
        return when {
            left is MInteger && right is MInteger -> evalIntegerInfixExpression(
                operator,
                left,
                right
            )
            operator == "==" -> (left == right).toMonkey()
            operator == "!=" -> (left != right).toMonkey()
            left.typeDesc() != right.typeDesc() -> MError("type mismatch: ${left.typeDesc()} $operator ${right.typeDesc()}")
            left is MString && right is MString -> evalStringInfixExpression(
                operator,
                left,
                right
            )
            else -> MError("unknown operator: ${left.typeDesc()} $operator ${right.typeDesc()}")
        }
    }

    private fun evalStringInfixExpression(operator: String, left: MString, right: MString): MObject {
        return if (operator != "+") {
            MError("unknown operator: ${left.typeDesc()} $operator ${right.typeDesc()}")
        } else {
            left + right
        }
    }

    private fun evalIntegerInfixExpression(operator: String, left: MInteger, right: MInteger): MObject {
        return when (operator) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> left / right
            "<" -> (left < right).toMonkey()
            ">" -> (left > right).toMonkey()
            "==" -> (left == right).toMonkey()
            "!=" -> (left != right).toMonkey()
            else -> MError("unknown operator: ${left.typeDesc()} $operator ${right.typeDesc()}")
        }
    }

    private fun evalPrefixExpression(operator: String, right: MObject?): MObject? {
        return when (operator) {
            "!" -> evalBangOperatorExpression(right)
            "-" -> evalMinusPrefixOperatorExpression(right)
            else -> MError("Unknown operator: $operator${right.typeDesc()}")
        }
    }

    private fun evalMinusPrefixOperatorExpression(right: MObject?): MObject? {
        return if (right != null) {
            if (right !is MInteger) {
                MError("unknown operator: -${right.typeDesc()}")
            } else {
                return -right

            }
        } else {
            null
        }
    }

    private fun evalBangOperatorExpression(right: MObject?): MObject {
        return when (right) {
            TRUE -> FALSE
            FALSE -> TRUE
            NULL -> TRUE
            else -> FALSE
        }
    }


    private fun MObject?.ifNotError(body: (MObject) -> MObject?): MObject? {
        return when {
            this != null -> when (this) {
                is MError -> this
                else -> body(this)
            }
            else -> this
        }
    }


    private fun evalProgram(statements: List<Statement>, env: Environment): MObject? {
        var result: MObject? = null

        statements.forEach { statement ->
            result = eval(statement, env)

            when (result) {
                is MReturnValue -> return (result as MReturnValue).value
                is MError -> return result
            }
        }
        return result
    }
}