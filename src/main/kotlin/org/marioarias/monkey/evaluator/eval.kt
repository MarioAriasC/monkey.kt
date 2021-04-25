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
                eval(node.function, env).ifNotError { function ->
                    val args = evalExpressions(node.arguments, env)
                    if (args.size == 1 && args.first().isError()) {
                        args.first()
                    } else {
                        applyFunction(function, args)
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
        
        val pairs = node.pairs.mapKeys { (keyNode, _) ->
            val key = eval(keyNode, env)
            if(key.isError()) {
                return key
            }
            when(key) {
                is Hashable<*> -> key
                else -> return MError("unusable as hash key: ${key?.type()}")
            }
        }.mapValues { (key, valueNode) ->
            val value = eval(valueNode, env)
            if(value.isError()){
                return value
            }
            HashPair(key, value!!)
        }.mapKeys { (key, _) ->
            key.hashKey()
        }
        return MHash(pairs)
    }

    private fun evalIndexExpression(left: MObject?, index: MObject?): MObject? {
        return when {
            left?.type() == ObjectType.ARRAY && index?.type() == ObjectType.INTEGER -> evalArrayIndexExpression(
                left,
                index
            )
            left?.type() == ObjectType.HASH -> evalHashIndexExpression(left, index!!)
            else -> MError("index operator not supported: ${left?.type()}")
        }
    }

    private fun evalHashIndexExpression(hash: MObject, index: MObject): MObject? {
        val hashObject = hash as MHash
        return when(index) {
            is Hashable<*> -> {
                val pair = hashObject.pairs[index.hashKey()]
                pair?.value ?: NULL
            }
            else -> MError("unusable as a hash key: ${index.type()}")
        }
    }

    private fun evalArrayIndexExpression(array: MObject, index: MObject): MObject? {
        val mArray = (array as MArray).elements
        val i = (index as MInteger).value
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
            else -> MError("not a function: ${function.type()}")
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
        this.type() == ObjectType.ERROR
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
                val type = (result as MObject).type()
                if (type == ObjectType.RETURN || type == ObjectType.ERROR) {
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
            left.type() == ObjectType.INTEGER && right.type() == ObjectType.INTEGER -> evalIntegerInfixExpression(
                operator,
                left,
                right
            )
            operator == "==" -> (left == right).toMonkey()
            operator == "!=" -> (left != right).toMonkey()
            left.type() != right.type() -> MError("type mismatch: ${left.type()} $operator ${right.type()}")
            left.type() == ObjectType.STRING && right.type() == ObjectType.STRING -> evalStringInfixExpression(
                operator,
                left,
                right
            )
            else -> MError("unknown operator: ${left.type()} $operator ${right.type()}")
        }
    }

    private fun evalStringInfixExpression(operator: String, left: MObject, right: MObject): MObject {
        return if (operator != "+") {
            MError("unknown operator: ${left.type()} $operator ${right.type()}")
        } else {
            (left as MString) + (right as MString)
        }
    }

    private fun evalIntegerInfixExpression(operator: String, left: MObject, right: MObject): MObject {
        val leftVal = left as MInteger
        val rightVal = right as MInteger
        return when (operator) {
            "+" -> leftVal + rightVal
            "-" -> leftVal - rightVal
            "*" -> leftVal * rightVal
            "/" -> leftVal / rightVal
            "<" -> (leftVal < rightVal).toMonkey()
            ">" -> (leftVal > rightVal).toMonkey()
            "==" -> (leftVal == rightVal).toMonkey()
            "!=" -> (leftVal != rightVal).toMonkey()
            else -> MError("unknown operator: ${left.type()} $operator ${right.type()}")
        }
    }

    private fun evalPrefixExpression(operator: String, right: MObject?): MObject? {
        return when (operator) {
            "!" -> evalBangOperatorExpression(right)
            "-" -> evalMinusPrefixOperatorExpression(right)
            else -> MError("Unknown operator: $operator${right?.type()}")
        }
    }

    private fun evalMinusPrefixOperatorExpression(right: MObject?): MObject? {
        return if (right != null) {
            if (right.type() != ObjectType.INTEGER) {
                MError("unknown operator: -${right.type()}")
            } else {
                return -(right as MInteger)

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
        return if (this != null) {
            if (this.type() == ObjectType.ERROR) {
                this
            } else {
                body(this)
            }
        } else {
            this
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