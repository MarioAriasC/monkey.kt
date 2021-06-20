package org.marioarias.monkey.code

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

typealias Instructions = ByteArray


fun Instructions.inspect(): String {
    val builder = StringBuilder()
    var i = 0
    while (i < size) {
        try {
            val def = lookup(this[i])
            val (operands, read) = readOperands(def, this.offset(i + 1))
            builder.append("%04d %s\n".format(i, fmtInstruction(def, operands)))
            i += 1 + read
        } catch (e: IllegalArgumentException) {
            builder.append("ERROR:${e.message}")
            continue
        }
    }
    return builder.toString()
}


fun ByteArray.offset(offset: Int): Instructions {
    return takeLast(size - offset).toByteArray()
}

private fun fmtInstruction(def: Definition, operands: IntArray): String {
    val operandCount = def.operandsWidths.size
    if (operands.size != operandCount) {
        return "ERROR: operand len ${operands.size} does not match defined $operandCount\n"
    }
    return when (operandCount) {
        0 -> def.name
        1 -> "${def.name} ${operands[0]}"
        else -> "ERROR: unhandled operandCount for ${def.name}"
    }
}


fun ByteArray.readChar(): Char {
    val stream = DataInputStream(this.inputStream())
    return stream.readChar().also { stream.close() }
}

fun ByteArray.writeChar(offset: Int, i: Int) {
    val byteStream = ByteArrayOutputStream(this.size)
    val stream = DataOutputStream(byteStream)
    stream.writeChar(i)
    stream.close()
    val newArray = byteStream.toByteArray()
    this[offset] = newArray[0]
    this[offset + 1] = newArray[1]
}

typealias Opcode = Byte

const val OpConstant: Opcode = 1
const val OpAdd: Opcode = 2
const val OpPop: Opcode = 3
const val OpSub: Opcode = 4
const val OpMul: Opcode = 5
const val OpDiv: Opcode = 6
const val OpTrue: Opcode = 7
const val OpFalse: Opcode = 8
const val OpEqual: Opcode = 9
const val OpNotEqual: Opcode = 10
const val OpGreaterThan: Opcode = 11
const val OpMinus: Opcode = 12
const val OpBang: Opcode = 13

val definitions: Map<Opcode, Definition> = mapOf(
    OpConstant to "OpConstant".toDefinition(intArrayOf(2)),
    OpAdd to "OpAdd".toDefinition(),
    OpPop to "OpPop".toDefinition(),
    OpSub to "OpSub".toDefinition(),
    OpMul to "OpMul".toDefinition(),
    OpDiv to "OpDiv".toDefinition(),
    OpTrue to "OpTrue".toDefinition(),
    OpFalse to "OpFalse".toDefinition(),
    OpEqual to "OpEqual".toDefinition(),
    OpNotEqual to "OpNotEqual".toDefinition(),
    OpGreaterThan to "OpGreaterThan".toDefinition(),
    OpMinus to "OpMinus".toDefinition(),
    OpBang to "OpBang".toDefinition(),
)

private fun String.toDefinition(operandsWidths: IntArray = intArrayOf()) = Definition(this, operandsWidths)

data class Definition(val name: String, val operandsWidths: IntArray = intArrayOf()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Definition) return false

        if (name != other.name) return false
        if (!operandsWidths.contentEquals(other.operandsWidths)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + operandsWidths.contentHashCode()
        return result
    }
}

fun readOperands(def: Definition, ins: Instructions): Pair<IntArray, Int> {
    var offset = 0
    val operands = def.operandsWidths.map { width ->
        when (width) {
            2 -> ins.offset(offset).readChar().code
            else -> width
        }.also { offset += width }
    }
    return operands.toIntArray() to offset
}

fun lookup(op: Opcode): Definition {
    return definitions.getOrElse(op) { throw IllegalArgumentException("opcode $op undefined") }
}

fun make(op: Opcode, vararg operands: Int): Instructions {
    return try {
        val def = lookup(op)

        val instructionLen = def.operandsWidths.sum() + 1
        val instruction = ByteArray(instructionLen)
        instruction[0] = op
        var offset = 1
        operands.forEachIndexed { i, operand ->
            val width = def.operandsWidths[i]
            when (width) {
                2 -> instruction.writeChar(offset, operand)
            }
            offset += width
        }
        instruction
    } catch (e: IllegalArgumentException) {
        byteArrayOf()
    }
}