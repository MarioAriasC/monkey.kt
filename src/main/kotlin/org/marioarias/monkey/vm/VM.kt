package org.marioarias.monkey.vm

import org.marioarias.monkey.code.*
import org.marioarias.monkey.compiler.Bytecode
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject


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

    fun run() {
        var i = 0
        while (i < instructions.size) {
            when (instructions[i]) {
                OpConstant -> {
                    val constIndex = instructions.offset(i + 1).readChar()
                    i += 2
                    push(constant[constIndex.code])
                }
                OpAdd -> {
                    val right = pop()
                    val left = pop()
                    val rightValue = (right as MInteger).value
                    val leftValue = (left as MInteger).value
                    val result = leftValue + rightValue
                    push(MInteger(result))
                }
            }
            i++
        }
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