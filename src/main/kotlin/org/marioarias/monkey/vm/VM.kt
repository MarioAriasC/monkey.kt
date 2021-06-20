package org.marioarias.monkey.vm

import org.marioarias.monkey.code.*
import org.marioarias.monkey.compiler.Bytecode
import org.marioarias.monkey.objects.MBoolean
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.ObjectType

val True = MBoolean(true)
val False = MBoolean(false)

class VM(bytecode: Bytecode) {
    private var constant: List<MObject> = bytecode.constants
    private var instructions: Instructions = bytecode.instructions
    private var stack: MutableList<MObject> = mutableListOf()
    private var sp: Int = 0

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
                    val constIndex = instructions.offset(i + 1).readChar()
                    i += 2
                    push(constant[constIndex.code])
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
            }
            i++
        }
    }

    private fun executeMinusOperator() {
        val operand = pop()
        if(operand!!.type() != ObjectType.INTEGER){
            throw VMException("unsupported type for negation: ${operand.type()}")
        }
        val value = (operand as MInteger).value
        push(MInteger(-value))
    }

    private fun executeBangOperator() {
        when(pop()){
            True -> push(False)
            False -> push(True)
            else -> push(False)
        }
    }

    private fun binaryOperationTemplate(
        op: Byte,
        intOperation: (op: Byte, left: MInteger, right: MInteger) -> Unit,
        otherOperation: (op: Byte, left: MObject, leftType: ObjectType, right: MObject, rightType: ObjectType) -> Unit
    ) {
        val right = pop()
        val left = pop()

        val leftType = left!!.type()
        val rightType = right!!.type()

        if (leftType == ObjectType.INTEGER && rightType == ObjectType.INTEGER) {
            intOperation(op, left as MInteger, right as MInteger)
        } else {
            otherOperation(op, left, leftType, right, rightType)
        }
    }

    private fun executeComparison(op: Byte) {
        binaryOperationTemplate(op, ::executeBinaryIntegerComparison) { op, left, leftType, right, rightType ->
            val bool = when(op) {
                OpEqual -> (left == right).toMBoolean()
                OpNotEqual -> (left != right).toMBoolean()
                else -> throw VMException("unknown operator $op ($leftType $rightType)")
            }
            push(bool)
        }
    }

    private fun executeBinaryIntegerComparison(op: Byte, left: MInteger, right: MInteger) {
        val leftValue = left.value
        val rightValue = right.value
        val bool = when(op){
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
        binaryOperationTemplate(op, ::executeBinaryIntegerOperation) { _, _, leftType, _, rightType ->
            throw VMException("unsupported types for binary operation: $leftType $rightType")
        }
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

class VMException(message: String) : Exception(message)