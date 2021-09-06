package org.marioarias.monkey.code

import org.marioarias.monkey.concat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CodeTests {
    @Test
    fun make() {
        listOf(
            Triple(OpConstant, intArrayOf(65534), byteArrayOf(OpConstant, 255.toByte(), 254.toByte())),
            Triple(OpAdd, intArrayOf(), byteArrayOf(OpAdd)),
            Triple(OpGetLocal, intArrayOf(255), byteArrayOf(OpGetLocal, 255.toByte()))
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
            make(OpConstant, 65535)
        )
        val expected = """0000 OpAdd
            |0001 OpGetLocal 1
            |0003 OpConstant 2
            |0006 OpConstant 65535
            |
        """.trimMargin()
        val instruction = instructions.concat()
        assertEquals(expected, instruction.inspect())
    }

    @Test
    fun `read operands`() {
        listOf(
            Triple(OpConstant, intArrayOf(65535), 2)
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