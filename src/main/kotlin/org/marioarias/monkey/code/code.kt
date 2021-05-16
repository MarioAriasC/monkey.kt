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

val definitions: Map<Opcode, Definition> = mapOf(
    OpConstant to Definition("OpConstant", intArrayOf(2)),
    OpAdd to Definition("OpAdd", intArrayOf())
)

data class Definition(val name: String, val operandsWidths: IntArray) {
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