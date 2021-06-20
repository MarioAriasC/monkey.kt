package org.marioarias.monkey.compiler

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.code.*
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject

class MCompiler {

    private var instructions: Instructions = byteArrayOf()
    private var constants: MutableList<MObject> = mutableListOf()

    @Throws(MCompilerException::class)
    fun compile(node: Node) {
        when (node) {
            is Program -> node.statements.forEach(this::compile)
            is ExpressionStatement -> {
                compile(node.expression!!)
                emit(OpPop)
            }
            is InfixExpression -> {
                if (node.operator == "<") {
                    compile(node.right!!)
                    compile(node.left!!)
                    emit(OpGreaterThan)
                    return
                }
                compile(node.left!!)
                compile(node.right!!)
                when (node.operator) {
                    "+" -> emit(OpAdd)
                    "-" -> emit(OpSub)
                    "*" -> emit(OpMul)
                    "/" -> emit(OpDiv)
                    ">" -> emit(OpGreaterThan)
                    "==" -> emit(OpEqual)
                    "!=" -> emit(OpNotEqual)
                    else -> throw MCompilerException("unknown operator ${node.operator}")
                }
            }
            is PrefixExpression -> {
                compile(node.right!!)
                when(node.operator){
                    "!" -> emit(OpBang)
                    "-" -> emit(OpMinus)
                    else -> throw MCompilerException("unknown operator ${node.operator}")
                }
            }
            is IntegerLiteral -> emit(OpConstant, addConstant(MInteger(node.value)))
            is BooleanLiteral -> {
                if (node.value) {
                    emit(OpTrue)
                } else {
                    emit(OpFalse)
                }
            }
        }
    }

    private fun addConstant(obj: MObject): Int {
        constants += obj
        return constants.size - 1
    }

    private fun addInstruction(ins: ByteArray): Int {
        val posNewInstruction = instructions.size
        instructions += ins
        return posNewInstruction
    }

    private fun emit(op: Opcode, vararg operands: Int): Int {
        val ins = make(op, *operands)
        return addInstruction(ins)
    }

    fun bytecode(): Bytecode = Bytecode(instructions, constants)
}

data class Bytecode(val instructions: Instructions, val constants: List<MObject>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bytecode) return false

        if (!instructions.contentEquals(other.instructions)) return false
        if (constants != other.constants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instructions.contentHashCode()
        result = 31 * result + constants.hashCode()
        return result
    }
}

class MCompilerException(message: String) : Exception(message)