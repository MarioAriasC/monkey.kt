@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.vm

import org.marioarias.monkey.code.*
import org.marioarias.monkey.compiler.Bytecode
import org.marioarias.monkey.objects.*

val True = MBoolean(true)
val False = MBoolean(false)
val Null = MNull

class VM(bytecode: Bytecode, private val globals: MutableList<MObject> = mutableListOf()) {
    private val constants: List<MObject> = bytecode.constants
    private val stack = Array<MObject?>(STACK_SIZE) { null }
    private val frames = Array<Frame?>(MAX_FRAME_SIZE) { null }
    private var sp: Int = 0
    private var frameIndex: Int = 1

    init {
        val mainFn = MCompiledFunction(bytecode.instructions)
        val mainClosure = MClosure(mainFn)
        val mainFrame = Frame(mainClosure, 0)
        frames[0] = mainFrame
//        println("constant = ${constant}")
    }


    private fun currentFrame(): Frame = frames[frameIndex - 1]!!

    private fun pushFrame(frame: Frame) {
        frames[frameIndex] = frame
        frameIndex++
    }

    private fun popFrame(): Frame {
        frameIndex--
        return frames[frameIndex]!!
    }

    private fun stackTop(): MObject? {
        return if (sp == 0) {
            null
        } else {
            stack[sp - 1]
        }
    }

    fun lastPoppedStackElem(): MObject? = stack.getOrNull(sp)

    fun run() {
        var ip: Int
        var ins: Instructions
        var op: Opcode
//        var cycles = 0
        while (currentFrame().ip < currentFrame().instructions().size - 1) {
//            cycles++
            currentFrame().ip++
            ip = currentFrame().ip
            ins = currentFrame().instructions()
//            println("--")
//            println(ins.inspect())
            op = ins[ip]

            when (op) {
                OpConstant -> {
                    val constIndex = ins.readInt(ip + 1)
                    currentFrame().ip += 2
                    push(constants[constIndex])
                }
                OpAdd, OpSub, OpMul, OpDiv -> executeBinaryOperation(op)
                OpTrue -> push(True)
                OpFalse -> push(False)
                OpPop -> pop()
                OpEqual, OpNotEqual, OpGreaterThan -> executeComparison(op)
                OpBang -> when (pop()) {
                    True -> push(False)
                    False -> push(True)
                    Null -> push(True)
                    else -> push(False)
                }
                OpMinus -> executeMinusOperator()
                OpJump -> {
                    val pos = ins.readInt(ip + 1)
                    currentFrame().ip = pos - 1
                }
                OpJumpNotTruthy -> {
                    val pos = ins.readInt(ip + 1)
                    currentFrame().ip += 2
                    val condition = pop()
                    if (!condition.isTruthy()) {
                        currentFrame().ip = pos - 1
                    }
                }
                OpNull -> push(Null)
                OpSetGlobal -> {
                    val globalIndex = ins.readInt(ip + 1)
                    currentFrame().ip += 2
                    globals.add(globalIndex, pop()!!)
                }
                OpGetGlobal -> {
                    val globalIndex = ins.readInt(ip + 1)
                    currentFrame().ip += 2
                    push(globals[globalIndex])
                }
                OpArray -> buildAndPush(ins, ip, ::buildArray)
                OpHash -> buildAndPush(ins, ip, ::buildHash)
                OpIndex -> {
                    val index = pop()
                    val left = pop()
                    executeIndexExpression(left!!, index!!)
                }
                OpCall -> {
                    val numArgs = ins.readByte(ip + 1)
                    currentFrame().ip++
                    executeCall(numArgs.toInt())
                }
                OpReturnValue -> {
                    val returnValue = pop()
                    val frame = popFrame()
                    sp = frame.basePointer - 1
                    push(returnValue!!)
                }
                OpReturn -> {
                    val frame = popFrame()
                    sp = frame.basePointer - 1
                    push(Null)
                }
                OpSetLocal -> {
                    val localIndex = ins.readByte(ip + 1)
                    currentFrame().ip++
                    val frame = currentFrame()
                    stack[frame.basePointer + localIndex.toInt()] = pop()
                }
                OpGetLocal -> {
                    val localIndex = ins.readByte(ip + 1)
                    currentFrame().ip++
                    val frame = currentFrame()
                    push(stack[frame.basePointer + localIndex.toInt()]!!)
                }
                OpGetBuiltin -> {
                    val builtIndex = ins.readByte(ip + 1)
                    currentFrame().ip++
                    val (_, definition) = builtins[builtIndex.toInt()]
                    push(definition)
                }
                OpClosure -> {
                    val constIndex = ins.readInt(ip + 1)
                    val numFree = ins.readByte(ip + 3)
                    currentFrame().ip += 3
                    pushClosure(constIndex, numFree.toInt())
                }
                OpGetFree -> {
                    val freeIndex = ins.readByte(ip + 1)
                    currentFrame().ip++
                    val currentClosure = currentFrame().cl
                    push(currentClosure.free[freeIndex.toInt()])
                }
                OpCurrentClosure -> {
                    val currentClosure = currentFrame().cl
                    push(currentClosure)
                }
            }
        }
//        println("cycles = $cycles")
    }

    private fun buildAndPush(ins: Instructions, ip: Int, build: (Int, Int) -> MObject) {
        val numElements = ins.readInt(ip + 1)
        currentFrame().ip += 2
        val col = build(sp - numElements, sp)
        sp -= numElements
        push(col)
    }


    private fun pushClosure(constIndex: Int, numFree: Int) {
        when (val constant = constants[constIndex]) {
            is MCompiledFunction -> {
                val free = Array(numFree) { i ->
                    stack[sp - numFree - i]!!
                }.toList()
                sp -= numFree
                val closure = MClosure(constant, free)
                push(closure)
            }
            else -> throw VMException("not a function $constant")
        }
    }

    private fun executeCall(numArgs: Int) {
        when (val callee = stack[sp - 1 - numArgs]) {
            is MClosure -> callClosure(callee, numArgs)
            is MBuiltinFunction -> callBuiltin(callee, numArgs)
            else -> throw VMException("calling non-function or non-built-in")
        }
    }

    private fun callBuiltin(builtin: MBuiltinFunction, numArgs: Int) {
        val args = stack.slice(sp - numArgs until sp)
        val result = builtin.fn(args)
        sp = sp - numArgs - 1
        if (result != null) {
            push(result)
        } else {
            push(Null)
        }
    }

    private fun callClosure(cl: MClosure, numArgs: Int) {
        if (cl.fn.numParameters != numArgs) {
            throw VMException("wrong number of arguments: want=${cl.fn.numParameters}, got=$numArgs")
        }
        val frame = Frame(cl, sp - numArgs)
        pushFrame(frame)
        sp = frame.basePointer + cl.fn.numLocals
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
        push(-operand)
    }


    private fun executeComparison(op: Opcode) {
        val right = pop()
        val left = pop()

        when {
            left is MInteger && right is MInteger -> executeBinaryIntegerComparison(op, left, right)
            else -> when (op) {
                OpEqual -> push((left == right).toMBoolean())
                OpNotEqual -> push((left != right).toMBoolean())
                else -> throw VMException("unknown operator $op (${left.typeDesc()} ${right.typeDesc()})")
            }
        }
    }

    private fun executeBinaryIntegerComparison(op: Opcode, left: MInteger, right: MInteger) {
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

    private fun executeBinaryOperation(op: Opcode) {
        val right = pop()
        val left = pop()

        when {
            left is MInteger && right is MInteger -> executeBinaryIntegerOperation(op, left, right)
            left is MString && right is MString -> executeBinaryStringOperation(op, left, right)
            else -> throw VMException("unsupported types for binary operation: ${left.typeDesc()} ${right.typeDesc()}")
        }
    }

    private fun executeBinaryStringOperation(op: Opcode, left: MString, right: MString) {
        when (op) {
            OpAdd -> push(MString(left.value + right.value))
            else -> throw VMException("unknown string operator: $op")
        }
    }

    private fun executeBinaryIntegerOperation(op: Opcode, left: MInteger, right: MInteger) {
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
        stack[sp] = obj
        sp++
    }


    companion object {
        const val STACK_SIZE = 2048
        const val MAX_FRAME_SIZE = 1024
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