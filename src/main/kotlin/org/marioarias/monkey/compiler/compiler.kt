package org.marioarias.monkey.compiler

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.code.*
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject

data class EmittedInstruction(val op: Opcode = 0, val position: Int = 0)

class MCompiler(
    private var constants: MutableList<MObject> = mutableListOf(),
    private val symbolTable: SymbolTable = SymbolTable()
) {

    private var instructions: Instructions = byteArrayOf()
    private var lastInstruction = EmittedInstruction()
    private var previousInstruction = EmittedInstruction()


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
                when (node.operator) {
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
            is IfExpression -> {
                compile(node.condition!!)
                val jumpNotTruthyPos = emit(OpJumpNotTruthy, 9999)
                compile(node.consequence!!)
                if (lastInstructionIsPop()) {
                    removeLastPop()
                }
                val jumpPos = emit(OpJump, 9999)

                val afterConsequencePos = instructions.size
                changeOperand(jumpNotTruthyPos, afterConsequencePos)
                if (node.alternative == null) {
                    emit(OpNull)
                } else {
                    compile(node.alternative)
                    if (lastInstructionIsPop()) {
                        removeLastPop()
                    }
                }
                val afterAlternativePos = instructions.size
                changeOperand(jumpPos, afterAlternativePos)
            }
            is BlockStatement -> node.statements!!.forEach { statement ->
                compile(statement!!)
            }
            is LetStatement -> {
                compile(node.value!!)
                val symbol = symbolTable.define(node.name.value)
                emit(OpSetGlobal, symbol.index)
            }
            is Identifier -> {
                val symbol = symbolTable.resolve(node.value)
                emit(OpGetGlobal, symbol.index)
            }
        }
    }

    private fun changeOperand(opPos: Int, operand: Int) {
        val op = instructions[opPos]
        val newInstruction = make(op, operand)
        replaceInstruction(opPos, newInstruction)
    }

    private fun replaceInstruction(pos: Int, newInstruction: Instructions) {
        for (i in newInstruction.indices) {
            instructions[pos + i] = newInstruction[i]
        }
    }

    private fun removeLastPop() {
        instructions = instructions.onset(lastInstruction.position)
        lastInstruction = previousInstruction
    }

    private fun lastInstructionIsPop(): Boolean {
        return lastInstruction.op == OpPop
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
        val pos = addInstruction(ins)
        setLastInstruction(op, pos)
        return pos
    }

    private fun setLastInstruction(op: Opcode, position: Int) {
        val previous = lastInstruction
        val last = EmittedInstruction(op, position)
        previousInstruction = previous
        lastInstruction = last

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