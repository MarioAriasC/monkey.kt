@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.code

import org.marioarias.monkey.concat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CodeTests {

    @Test
    fun make() {
        listOf(
            Triple(OpConstant, intArrayOf(65534), ubyteArrayOf(OpConstant, 255u, 254u)),
            Triple(OpAdd, intArrayOf(), ubyteArrayOf(OpAdd)),
            Triple(OpGetLocal, intArrayOf(255), ubyteArrayOf(OpGetLocal, 255u))
        ).forEach { (op, operands, expected) ->
            val instruction = make(op, *operands)
            assertEquals(expected.size, instruction.size)
            expected.forEachIndexed { i, byte ->
                assertEquals(byte, instruction[i])
            }
        }
    }

    @Test
    fun `instructions inspect()`() {
        val instructions = listOf(
            make(OpAdd),
            make(OpGetLocal, 1),
            make(OpConstant, 2),
            make(OpConstant, 65535),
            make(OpClosure, 65535, 255)
        )
        val expected = """0000 OpAdd
            |0001 OpGetLocal 1
            |0003 OpConstant 2
            |0006 OpConstant 65535
            |0009 OpClosure 65535 255
            |
        """.trimMargin()
        val instruction = instructions.concat()
        assertEquals(expected, instruction.inspect())
    }

    @Test
    fun `read operands`() {
        listOf(
            Triple(OpConstant, intArrayOf(65535), 2),
            Triple(OpGetLocal, intArrayOf(255), 1),
            Triple(OpClosure, intArrayOf(65535, 255), 3)
        ).forEach { (op, operands, bytesRead) ->
            val instruction = make(op, *operands)
            try {
                val def = lookup(op)
                val (operandsRead, n) = readOperands(def, instruction.offset(1))
                assertEquals(bytesRead, n)
                operands.forEachIndexed { i, want ->
                    assertEquals(want, operandsRead[i])
                }
            } catch (e: Exception) {
                fail("definition not found ${e.message}")
            }
        }
    }
}

fun Instructions.inspect(): String {

    fun fmtInstruction(def: Definition, operands: IntArray): String {
        val operandCount = def.operandsWidths.size
        if (operands.size != operandCount) {
            return "ERROR: operand len ${operands.size} does not match defined $operandCount\n"
        }
        return when (operandCount) {
            0 -> def.name
            1 -> "${def.name} ${operands[0]}"
            2 -> "${def.name} ${operands[0]} ${operands[1]}"
            else -> "ERROR: unhandled operandCount for ${def.name}"
        }
    }

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
//            continue
        }
    }
    return builder.toString()
}

fun readOperands(def: Definition, ins: Instructions): Pair<IntArray, Int> {
    var offset = 0
    val operands = def.operandsWidths.map { width ->
        when (width) {
            2 -> ins.readInt(offset)
            1 -> ins.offset(offset).readByte().toInt()
            else -> width
        }.also { offset += width }
    }
    return operands.toIntArray() to offset
}

