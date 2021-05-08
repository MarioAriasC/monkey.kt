package org.marioarias.monkey.evaluator

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.evaluator.Environment.Companion.newEnclosedEnvironment
import org.marioarias.monkey.evaluator.Evaluator.eval
import org.marioarias.monkey.objects.MMacro
import org.marioarias.monkey.objects.MQuote

fun defineMacros(program: Program, env: Environment): Program {
    val newStatements = program.statements.groupBy { statement -> statement.isMacroDefinition() }
    newStatements[true]?.forEach { statement -> addMacro(statement, env) }
    return Program(newStatements.getOrDefault(false, emptyList()))
}

fun expandMacros(program: Node, env: Environment): Node? {
    return modify(program) { node ->
        when (node) {
            is CallExpression -> {
                val (macro, isMacro) = node.isMacroCall(env)
                if (!isMacro) {
                    node
                } else {
                    val args = node.arguments?.map { args ->
                        MQuote(args)
                    }
                    val evalEnv = extendMacroEnv(macro, args)

                    val evaluated = eval(macro?.body, evalEnv)
                    if (evaluated !is MQuote) {
                        throw IllegalArgumentException("we only support returning AST-nodes from macros")
                    } else {
                        evaluated.node
                    }

                }
            }
            else -> node
        }
    }
}

fun extendMacroEnv(macro: MMacro?, args: List<MQuote>?): Environment {
    val extended = newEnclosedEnvironment(macro!!.env)
    macro.parameters?.forEachIndexed { i, parameter ->
        extended[parameter.value] = args!![i]
    }
    return extended
}

private fun CallExpression.isMacroCall(env: Environment): Pair<MMacro?, Boolean> {
    return when (this.function) {
        is Identifier -> {
            val obj = env[this.function.value]
            when {
                obj != null -> {
                    if (obj is MMacro) {
                        obj to true
                    } else {
                        null to false
                    }
                }
                else -> {
                    null to false
                }
            }
        }
        else -> {
            null to false
        }
    }
}

fun addMacro(statement: Statement, env: Environment) {
    val letStatement = statement as LetStatement
    val macroLiteral = letStatement.value as MacroLiteral
    env[letStatement.name.value] = with(macroLiteral) {
        MMacro(parameters, body, env)
    }
}

private fun Statement.isMacroDefinition(): Boolean {
    return when (this) {
        is LetStatement -> {
            this.value is MacroLiteral
        }
        else -> false
    }
}
