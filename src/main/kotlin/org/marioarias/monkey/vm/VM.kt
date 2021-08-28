package org.marioarias.monkey.vm

import org.marioarias.monkey.code.*
import org.marioarias.monkey.compiler.Bytecode
import org.marioarias.monkey.objects.*

val True = MBoolean(true)
val False = MBoolean(false)
val Null = MNull

class VM(bytecode: Bytecode) {
    private var constant: List<MObject> = bytecode.constants
    private var instructions: Instructions = bytecode.instructions
    private var stack: MutableList<MObject?> = Array<MObject?>(STACK_SIZE) { null }.toMutableList()
    private var sp: Int = 0
    private var globals: MutableList<MObject> = mutableListOf()

    constructor(bytecode: Bytecode, globals: MutableList<MObject>) : this(bytecode) {
        this.globals = globals
    }

    fun stackTop(): MObject? {
        return if (sp == 0) {
            null
        } else {
            stack[sp - 1]
        }
    }

    fun lastPoppedStackElem(): MObject? {
        return stack.getOrNull(sp)
    }

    fun run() {
        var i = 0
        while (i < instructions.size) {
            when (val op = instructions[i]) {
                OpConstant -> {
                    val constIndex = instructions.readInt(i + 1)
                    i += 2
                    push(constant[constIndex])
                }
                OpAdd, OpSub, OpMul, OpDiv -> {
                    executeBinaryOperation(op)
                }
                OpTrue -> push(True)
                OpFalse -> push(False)
                OpPop -> pop()
                OpEqual, OpNotEqual, OpGreaterThan -> {
                    executeComparison(op)
                }
                OpBang -> executeBangOperator()
                OpMinus -> executeMinusOperator()
                OpJump -> {
                    val pos = instructions.readInt(i + 1)
                    i = pos - 1
                }
                OpJumpNotTruthy -> {
                    val pos = instructions.readInt(i + 1)
                    i += 2
                    val condition = pop()
                    if (!condition.isTruthy()) {
                        i = pos - 1
                    }
                }
                OpNull -> {
                    push(Null)
                }
                OpSetGlobal -> {
                    val globalIndex = instructions.readInt(i + 1)
                    i += 2
                    globals.add(globalIndex, pop()!!)
                }
                OpGetGlobal -> {
                    val globalIndex = instructions.readInt(i + 1)
                    i += 2
                    push(globals[globalIndex])
                }
                OpArray -> {
                    val numElements = instructions.readInt(i + 1)
                    i += 2
                    val array = buildArray(sp - numElements, sp)
                    sp -= numElements
                    push(array)
                }
                OpHash -> {
                    val numElements = instructions.readInt(i + 1)
                    i += 2
                    val hash = buildHash(sp - numElements, sp)
                    sp -= numElements
                    push(hash)
                }
                OpIndex -> {
                    val index = pop()
                    val left = pop()
                    executeIndexExpression(left!!, index!!)
                }
            }
            i++
        }
    }

    private fun executeIndexExpression(left: MObject, index: MObject) {
        when {
            left is MArray && index is MInteger -> executeArrayIndex(left, index)
            left is MHash -> executeHashIndex(left, index)
            else -> throw VMException("index operator not supported ${left.typeDesc()}")
        }
    }

    private fun executeHashIndex(hash: MHash, index: MObject) {
        when (index) {
            is Hashable<*> -> {
                when (val pair = hash.pairs[index.hashKey()]) {
                    null -> push(Null)
                    else -> push(pair.value)
                }
            }
            else -> throw VMException("unusable as hash key: ${index.typeDesc()}")
        }
    }

    private fun executeArrayIndex(array: MArray, index: MInteger) {
        val i = index.value
        val max = array.elements.size - 1L
        if (i < 0 || i > max) {
            push(Null)
        } else {
            push(array.elements[i.toInt()]!!)
        }
    }

    private fun buildHash(startIndex: Int, endIndex: Int): MObject {
        val hashedPairs = mutableMapOf<HashKey, HashPair>()
        for (i in startIndex until endIndex step 2) {
            val key = stack[i]
            val value = stack[i + 1]
            if (key == null || value == null) {
                continue
            }
            val pair = HashPair(key, value)
            when (key) {
                is Hashable<*> -> hashedPairs[key.hashKey()] = pair
                else -> throw VMException("unusable as hash key: ${key.typeDesc()}")
            }
        }
        return MHash(hashedPairs)
    }

    private fun buildArray(startIndex: Int, endIndex: Int): MObject {
        val elements = Array<MObject?>(endIndex - startIndex) { null }.toMutableList()
        var i = startIndex
        while (i < endIndex) {
            elements[i - startIndex] = stack[i]
            i++
        }
        return MArray(elements)
    }

    private fun executeMinusOperator() {
        val operand = pop()
        if (operand !is MInteger) {
            throw VMException("unsupported type for negation: ${operand.typeDesc()}")
        }
        val value = operand.value
        push(MInteger(-value))
    }

    private fun executeBangOperator() {
        when (pop()) {
            True -> push(False)
            False -> push(True)
            Null -> push(True)
            else -> push(False)
        }
    }

    private fun binaryOperationTemplate(
        op: Byte,
        intOperation: (op: Byte, left: MInteger, right: MInteger) -> Unit,
        stringOperation: (op: Byte, left: MString, right: MString) -> Unit = { _, _, _ -> },
        otherOperation: (op: Byte, left: MObject, right: MObject) -> Unit
    ) {
        val right = pop()
        val left = pop()

        when {
            left is MInteger && right is MInteger -> {
                intOperation(op, left, right)
            }
            left is MString && right is MString -> {
                stringOperation(op, left, right)
            }
            else -> otherOperation(op, left!!, right!!)

        }
    }

    private fun executeComparison(op: Byte) {
        binaryOperationTemplate(op, ::executeBinaryIntegerComparison) { opCode, left, right ->
            val bool = when (opCode) {
                OpEqual -> (left == right).toMBoolean()
                OpNotEqual -> (left != right).toMBoolean()
                else -> throw VMException("unknown operator $opCode (${left.typeDesc()} ${right.typeDesc()})")
            }
            push(bool)
        }
    }

    private fun executeBinaryIntegerComparison(op: Byte, left: MInteger, right: MInteger) {
        val leftValue = left.value
        val rightValue = right.value
        val bool = when (op) {
            OpEqual -> (leftValue == rightValue).toMBoolean()
            OpNotEqual -> (leftValue != rightValue).toMBoolean()
            OpGreaterThan -> (leftValue > rightValue).toMBoolean()
            else -> throw VMException("unknown operator $op")
        }
        push(bool)
    }

    private fun Boolean.toMBoolean() = if (this) {
        True
    } else {
        False
    }

    private fun executeBinaryOperation(op: Byte) {
        binaryOperationTemplate(
            op,
            ::executeBinaryIntegerOperation,
            ::executeBinaryStringOperation
        ) { _, left, right ->
            throw VMException("unsupported types for binary operation: ${left.typeDesc()} ${right.typeDesc()}")
        }
    }

    private fun executeBinaryStringOperation(op: Byte, left: MString, right: MString) {
        val result = when (op) {
            OpAdd -> MString(left.value + right.value)
            else -> throw VMException("unknown string operator: $op")
        }
        push(result)
    }

    private fun executeBinaryIntegerOperation(op: Byte, left: MInteger, right: MInteger) {
        val result = when (op) {
            OpAdd -> left + right
            OpSub -> left - right
            OpMul -> left * right
            OpDiv -> left / right
            else -> throw VMException("unknown integer operator $op")
        }
        push(result)
    }

    private fun pop(): MObject? {
        return stackTop().also { sp-- }
    }

    private fun push(obj: MObject) {
        if (sp >= STACK_SIZE) {
            throw VMException("stack overflow")
        }
        stack.add(sp, obj)
        sp++
    }


    companion object {
        const val STACK_SIZE = 2048
    }


}

private fun MObject?.isTruthy(): Boolean {
    return when (this) {
        is MBoolean -> value
        is MNull -> false
        else -> true
    }
}

class VMException(message: String) : Exception(message)