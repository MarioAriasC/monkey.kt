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
            left.type() == ObjectType.ARRAY && index.type() == ObjectType.INTEGER -> executeArrayIndex(left, index)
            left.type() == ObjectType.HASH -> executeHashIndex(left, index)
            else -> throw VMException("index operator not supported ${left::class}")
        }
    }

    private fun executeHashIndex(hash: MObject, index: MObject) {
        val hashObject = hash as MHash
        when (index) {
            is Hashable<*> -> {
                when (val pair = hashObject.pairs[index.hashKey()]) {
                    null -> push(Null)
                    else -> push(pair.value)
                }
            }
            else -> throw VMException("unusable as hash key: ${index::class}")
        }
    }

    private fun executeArrayIndex(array: MObject, index: MObject) {
        val arrayObject = array as MArray
        val i = (index as MInteger).value
        val max = arrayObject.elements.size - 1L
        if (i < 0 || i > max) {
            push(Null)
        } else {
            push(arrayObject.elements[i.toInt()]!!)
        }
    }

    private fun buildHash(startIndex: Int, endIndex: Int): MObject {
        val hashedPairs = mutableMapOf<HashKey, HashPair>()
        for (i in startIndex until endIndex step 2) {
            val key = stack[i]
            val value = stack[i + 1]
            if (key ==  null || value == null){
                continue
            }
            val pair = HashPair(key, value)
            when (key) {
                is Hashable<*> -> hashedPairs[key.hashKey()] = pair
                else -> throw VMException("unusable as hash key: ${key.type()}")
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
        if (operand!!.type() != ObjectType.INTEGER) {
            throw VMException("unsupported type for negation: ${operand.type()}")
        }
        val value = (operand as MInteger).value
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
        otherOperation: (op: Byte, left: MObject, leftType: ObjectType, right: MObject, rightType: ObjectType) -> Unit
    ) {
        val right = pop()
        val left = pop()

        val leftType = left!!.type()
        val rightType = right!!.type()

        when {
            leftType == ObjectType.INTEGER && rightType == ObjectType.INTEGER -> {
                intOperation(op, left as MInteger, right as MInteger)
            }
            leftType == ObjectType.STRING && rightType == ObjectType.STRING -> {
                stringOperation(op, left as MString, right as MString)
            }
            else -> otherOperation(op, left, leftType, right, rightType)

        }
    }

    private fun executeComparison(op: Byte) {
        binaryOperationTemplate(op, ::executeBinaryIntegerComparison) { opCode, left, leftType, right, rightType ->
            val bool = when (opCode) {
                OpEqual -> (left == right).toMBoolean()
                OpNotEqual -> (left != right).toMBoolean()
                else -> throw VMException("unknown operator $opCode ($leftType $rightType)")
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
        ) { _, _, leftType, _, rightType ->
            throw VMException("unsupported types for binary operation: $leftType $rightType")
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