@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.code

typealias Instructions = UByteArray


fun Instructions.offset(offset: Int): Instructions = copyOfRange(offset, size)

fun Instructions.onset(onset: Int): Instructions = copyOfRange(0, onset)

fun Instructions.readInt(offset: Int): Int {
    return offset(offset).readChar().code
}

fun Instructions.readByte(offset: Int): UByte {
    return offset(offset).readByte()
}

fun Instructions.read(position: Int): Int = (this[position] and 255u).toInt()

fun Instructions.readChar(): Char {
    val ch1 = read(0)
    val ch2 = read(1)
    if ((ch1 or ch2) < 0) {
        throw IllegalStateException()
    } else {
        return ((ch1 shl 8) + (ch2 shl 0)).toChar()
    }
}

fun Instructions.readByte(): UByte {
    val ch = read(0)
    if (ch < 0) {
        throw IllegalStateException()
    } else {
        return ch.toUByte()
    }
}

private fun Instructions.writeChar(offset: Int, i: Int) {
    this[offset] = ((i ushr 8) and 255).toUByte()
    this[offset + 1] = ((i ushr 0) and 255).toUByte()
}

typealias Opcode = UByte

const val OpConstant: Opcode = 0u
const val OpAdd: Opcode = 1u
const val OpPop: Opcode = 2u
const val OpSub: Opcode = 3u
const val OpMul: Opcode = 4u
const val OpDiv: Opcode = 5u
const val OpTrue: Opcode = 6u
const val OpFalse: Opcode = 7u
const val OpEqual: Opcode = 8u
const val OpNotEqual: Opcode = 9u
const val OpGreaterThan: Opcode = 10u
const val OpMinus: Opcode = 11u
const val OpBang: Opcode = 12u
const val OpJumpNotTruthy: Opcode = 13u
const val OpJump: Opcode = 14u
const val OpNull: Opcode = 15u
const val OpGetGlobal: Opcode = 16u
const val OpSetGlobal: Opcode = 17u
const val OpArray: Opcode = 18u
const val OpHash: Opcode = 19u
const val OpIndex: Opcode = 20u
const val OpCall: Opcode = 21u
const val OpReturnValue: Opcode = 22u
const val OpReturn: Opcode = 23u
const val OpGetLocal: Opcode = 24u
const val OpSetLocal: Opcode = 25u
const val OpGetBuiltin: Opcode = 26u
const val OpClosure: Opcode = 27u
const val OpGetFree: Opcode = 28u
const val OpCurrentClosure: Opcode = 29u


val defOpConstant = "OpConstant".toDefinition(2)
val defOpAdd = "OpAdd".toDefinition()
val defOpPop = "OpPop".toDefinition()
val defOpSub = "OpSub".toDefinition()
val defOpMul = "OpMul".toDefinition()
val defOpDiv = "OpDiv".toDefinition()
val defOpTrue = "OpTrue".toDefinition()
val defOpFalse = "OpFalse".toDefinition()
val defOpEqual = "OpEqual".toDefinition()
val defOpNotEqual = "OpNotEqual".toDefinition()
val defOpGreaterThan = "OpGreaterThan".toDefinition()
val defOpMinus = "OpMinus".toDefinition()
val defOpBang = "OpBang".toDefinition()
val defOpJumpNotTruthy = "OpJumpNotTruthy".toDefinition(2)
val defOpJump = "OpJump".toDefinition(2)
val defOpNull = "OpNull".toDefinition()
val defOpGetGlobal = "OpGetGlobal".toDefinition(2)
val defOpSetGlobal = "OpSetGlobal".toDefinition(2)
val defOpArray = "OpArray".toDefinition(2)
val defOpHash = "OpHash".toDefinition(2)
val defOpIndex = "OpIndex".toDefinition()
val defOpCall = "OpCall".toDefinition(1)
val defOpReturnValue = "OpReturnValue".toDefinition()
val defOpReturn = "OpReturn".toDefinition()
val defOpGetLocal = "OpGetLocal".toDefinition(1)
val defOpSetLocal = "OpSetLocal".toDefinition(1)
val defOpGetBuiltin = "OgGetBuiltin".toDefinition(1)
val defOpClosure = "OpClosure".toDefinition(2, 1)
val defOpGetFree = "OpGetFree".toDefinition(1)
val defOpCurrentClosure = "OpCurrentClosure".toDefinition()

private fun String.toDefinition(vararg operandsWidths: Int) = Definition(this, intArrayOf(*operandsWidths))

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

fun lookup(op: Opcode): Definition {
//    return definitions.getOrElse(op) { throw IllegalArgumentException("opcode $op undefined") }
    return when (op) {
        OpConstant -> defOpConstant
        OpAdd -> defOpAdd
        OpPop -> defOpPop
        OpSub -> defOpSub
        OpMul -> defOpMul
        OpDiv -> defOpDiv
        OpTrue -> defOpTrue
        OpFalse -> defOpFalse
        OpEqual -> defOpEqual
        OpNotEqual -> defOpNotEqual
        OpGreaterThan -> defOpGreaterThan
        OpMinus -> defOpMinus
        OpBang -> defOpBang
        OpJumpNotTruthy -> defOpJumpNotTruthy
        OpJump -> defOpJump
        OpNull -> defOpNull
        OpGetGlobal -> defOpGetGlobal
        OpSetGlobal -> defOpSetGlobal
        OpArray -> defOpArray
        OpHash -> defOpHash
        OpIndex -> defOpIndex
        OpCall -> defOpCall
        OpReturnValue -> defOpReturnValue
        OpReturn -> defOpReturn
        OpGetLocal -> defOpGetLocal
        OpSetLocal -> defOpSetLocal
        OpGetBuiltin -> defOpGetBuiltin
        OpClosure -> defOpClosure
        OpGetFree -> defOpGetFree
        OpCurrentClosure -> defOpCurrentClosure
        else -> throw IllegalArgumentException("opcode $op undefined")
    }
}

fun make(op: Opcode, vararg operands: Int): Instructions {
    return try {
        val def = lookup(op)

        val instructionLen = def.operandsWidths.sum() + 1
        val instruction = UByteArray(instructionLen)
        instruction[0] = op
        var offset = 1
        operands.forEachIndexed { i, operand ->
            val width = def.operandsWidths[i]
            when (width) {
                2 -> instruction.writeChar(offset, operand)
                1 -> instruction[offset] = operand.toUByte()
            }
            offset += width
        }
        instruction
    } catch (_: IllegalArgumentException) {
        ubyteArrayOf()
    }
}