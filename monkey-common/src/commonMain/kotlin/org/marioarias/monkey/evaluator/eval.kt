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


    private fun eval(expression: IndexExpression, env: Environment): MObject? {
        val left = eval(expression.left, env)

        if (left.isError()) {
            return left
        }

        val index = eval(expression.index, env)

        if (index.isError()) {
            return index
        }

        return when {
            left is MArray && index is MInteger -> evalArrayIndexExpression(
                left,
                index
            )

            left is MHash -> when (index!!) {
                is Hashable<*> -> {
                    val pair = left.pairs[index.hashKey()]
                    pair?.value ?: NULL
                }

                else -> MError("unusable as a hash key: ${index.typeDesc()}")
            }

            else -> MError("index operator not supported: ${left.typeDesc()}")
        }
    }


    private fun eval(expression: Expression?, env: Environment): MObject? = when (expression) {
        is ExpressionStatement -> eval(expression.expression, env)
        is LetStatement -> eval(expression.value, env).ifNotError { value ->
            env.put(expression.name.value, value)
        }

        is BlockStatement -> evalBlockStatement(expression, env)
        is ReturnStatement -> eval(expression.returnValue, env).ifNotError { value ->
            MReturnValue(value)
        }

        is IntegerLiteral -> MInteger(expression.value)
        is PrefixExpression -> eval(expression.right, env).ifNotError { right ->
            when (expression.operator) {
                "!" -> when (right) {
                    TRUE -> FALSE
                    FALSE -> TRUE
                    NULL -> TRUE
                    else -> FALSE
                }

                "-" -> if (right !is MInteger) {
                    MError("unknown operator: -${right.typeDesc()}")
                } else {
                    -right
                }

                else -> MError("Unknown operator: ${expression.operator}${right.typeDesc()}")
            }
        }

        is InfixExpression -> eval(expression.left, env).ifNotError { left ->
            eval(expression.right, env).ifNotError { right ->
                when {
                    left is MInteger && right is MInteger -> when (expression.operator) {
                        "+" -> left + right
                        "-" -> left - right
                        "*" -> left * right
                        "/" -> left / right
                        "<" -> (left < right).toMonkey()
                        ">" -> (left > right).toMonkey()
                        "==" -> (left == right).toMonkey()
                        "!=" -> (left != right).toMonkey()
                        else -> MError("unknown operator: ${left.typeDesc()} ${expression.operator} ${right.typeDesc()}")
                    }

                    expression.operator == "==" -> (left == right).toMonkey()
                    expression.operator == "!=" -> (left != right).toMonkey()
                    left.typeDesc() != right.typeDesc() -> MError("type mismatch: ${left.typeDesc()} ${expression.operator} ${right.typeDesc()}")
                    left is MString && right is MString && expression.operator == "+" -> left + right
                    else -> MError("unknown operator: ${left.typeDesc()} ${expression.operator} ${right.typeDesc()}")
                }
            }
        }

        is BooleanLiteral -> expression.value.toMonkey()
        is IfExpression -> {
            eval(expression.condition, env).ifNotError { condition ->
                when {
                    when (condition) {
                        NULL, FALSE -> false
                        else -> true
                    } -> evalBlockStatement(expression.consequence!!, env)

                    expression.alternative != null -> evalBlockStatement(expression.alternative, env)
                    else -> NULL
                }
            }
        }

        is Identifier -> when (val value = env[expression.value]) {
            null -> {
                when (val builtin = builtins[expression.value]) {
                    null -> MError("identifier not found: ${expression.value}")
                    else -> builtin
                }
            }

            else -> value
        }

        is StringLiteral -> MString(expression.value)
        is FunctionLiteral -> MFunction(expression.parameters, expression.body, env)
        is CallExpression -> eval(expression.function, env).ifNotError { function ->
            val args = evalExpressions(expression.arguments, env)
            if (args.size == 1 && args.first().isError()) {
                args.first()
            } else {
                when (function) {
                    is MFunction -> {
                        val innerEnv = Environment.newEnclosedEnvironment(function.env)
                        function.parameters?.forEachIndexed { i, identifier ->
                            innerEnv[identifier.value] = args[i]!!
                        }
                        when (val evaluated = eval(function.body, innerEnv)) {
                            is MReturnValue -> evaluated.value
                            else -> evaluated
                        }
                    }

                    is MBuiltinFunction -> function.fn(args) ?: MNull

                    else -> MError("not a function: ${function.typeDesc()}")
                }
            }
        }

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

    private fun evalArrayIndexExpression(array: MArray, index: MInteger): MObject? {
        val elements = array.elements
        val i = index.value
        val max = elements.size - 1

        if (i < 0 || i > max) {
            return NULL
        }

        return elements[i.toInt()]

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


    private fun MObject?.ifNotError(body: (MObject) -> MObject?): MObject? = when {
        this != null -> when (this) {
            is MError -> this
            else -> body(this)
        }

        else -> this
    }


}