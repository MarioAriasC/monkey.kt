package org.marioarias.monkey.evaluator

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.objects.*

object Evaluator {
    //engine=eval, result=9227465, duration=6.199426516s
    val NULL = MNull
    val TRUE = MBoolean(true)
    val FALSE = MBoolean(false)

    private fun Boolean.toMonkey(): MBoolean = if (this) TRUE else FALSE

    fun eval(program: Program, env: Environment): MObject? {
        var result: MObject? = null
        for (statement in program.statements) {
            result = eval(statement, env)

            when (result) {
                is MReturnValue -> return result.value
                is MError -> return result
            }
        }
        return result
    }

//    private fun eval(statement: ExpressionStatement, env: Environment) = eval(statement.expression, env)

    /*private fun eval(statement: Statement?, env: Environment): MObject? = when (statement) {
        is ExpressionStatement -> eval(statement.expression, env)
        is LetStatement -> eval(statement, env)
        is BlockStatement -> evalBlockStatement(statement, env)
        is ReturnStatement -> eval(statement, env)
        else -> null
    }*/


    private fun eval(expression: IndexExpression, env: Environment): MObject? {
        val left = eval(expression.left, env)

        if (left.isError()) {
            return left
        }

        val index = eval(expression.index, env)

        if (index.isError()) {
            return index
        }

        return evalIndexExpression(left, index)
    }


    private fun eval(expression: Expression?, env: Environment): MObject? = when (expression) {
        is Identifier -> when (val value = env[expression.value]) {
            null -> {
                when (val builtin = builtins[expression.value]) {
                    null -> MError("identifier not found: ${expression.value}")
                    else -> builtin
                }
            }

            else -> value
        }

        is IntegerLiteral -> MInteger(expression.value)
        is InfixExpression -> eval(expression.left, env).ifNotError { left ->
            eval(expression.right, env).ifNotError { right ->
                evalInfixExpression(expression.operator, left, right)
            }
        }

        is BlockStatement -> evalBlockStatement(expression, env)
        is ExpressionStatement -> eval(expression.expression, env)
        is IfExpression -> {
            fun isTruthy(obj: MObject): Boolean {
                return when (obj) {
                    NULL -> false
                    TRUE -> true
                    FALSE -> false
                    else -> true
                }
            }
            eval(expression.condition, env).ifNotError { condition ->
                when {
                    isTruthy(condition) -> evalBlockStatement(expression.consequence!!, env)
                    expression.alternative != null -> evalBlockStatement(expression.alternative, env)
                    else -> NULL
                }
            }
        }

        is CallExpression -> eval(expression.function, env).ifNotError { function ->
            val args = evalExpressions(expression.arguments, env)
            if (args.size == 1 && args.first().isError()) {
                args.first()
            } else {
                applyFunction(function, args)
            }
        }

        is ReturnStatement -> eval(expression.returnValue, env).ifNotError { value ->
            MReturnValue(value)
        }

        is PrefixExpression -> eval(expression.right, env).ifNotError { right ->
            when (expression.operator) {
                "!" -> evalBangOperatorExpression(right)
                "-" -> evalMinusPrefixOperatorExpression(right)
                else -> MError("Unknown operator: ${expression.operator}${right.typeDesc()}")
            }
        }

        is BooleanLiteral -> expression.value.toMonkey()
        is LetStatement -> eval(expression.value, env).ifNotError { value ->
            env.put(expression.name.value, value)
        }

        is FunctionLiteral -> MFunction(expression.parameters, expression.body, env)
        is StringLiteral -> MString(expression.value)
        is ArrayLiteral -> {
            val elements = evalExpressions(expression.elements, env)
            if (elements.size == 1 && elements.first().isError()) {
                elements.first()
            } else {
                MArray(elements)
            }
        }

        is IndexExpression -> eval(expression, env)
        is HashLiteral -> {
            val pairs = mutableMapOf<HashKey, HashPair>()
            expression.pairs.forEach { (keyNode, valueNode) ->
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
            MHash(pairs)
        }

        else -> null
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
        val elements = array.elements
        val i = index.value
        val max = elements.size - 1

        if (i < 0 || i > max) {
            return NULL
        }

        return elements[i.toInt()]

    }

    private fun applyFunction(function: MObject, args: List<MObject?>): MObject? {
        return when (function) {
            is MFunction -> {
                val extendEnv = extendFunctionEnv(function, args)
                when (val evaluated = eval(function.body, extendEnv)) {
                    is MReturnValue -> evaluated.value
                    else -> evaluated
                }
            }

            is MBuiltinFunction -> function.fn(args) ?: MNull

            else -> MError("not a function: ${function.typeDesc()}")
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

    private fun evalExpressions(arguments: List<Expression?>?, env: Environment): List<MObject?> =
        arguments!!.map { argument ->
            val evaluated = eval(argument, env)
            if (evaluated.isError()) {
                return listOf(evaluated)
            }
            evaluated
        }

    private fun evalBlockStatement(node: BlockStatement, env: Environment): MObject? {
        var result: MObject? = null

        if (node.statements != null) {
            for (statement in node.statements) {
                result = eval(statement, env)

                if (result != null) {
                    //val type = (result as MObject)
                    if (result is MReturnValue || result is MError) {
                        return result
                    }
                }
            }
        }
        return result
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
            left is MString && right is MString && operator == "+" -> left + right
            else -> MError("unknown operator: ${left.typeDesc()} $operator ${right.typeDesc()}")
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

    private fun evalMinusPrefixOperatorExpression(right: MObject): MObject = if (right !is MInteger) {
        MError("unknown operator: -${right.typeDesc()}")
    } else {
        -right
    }

    private fun evalBangOperatorExpression(right: MObject): MObject {
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


}