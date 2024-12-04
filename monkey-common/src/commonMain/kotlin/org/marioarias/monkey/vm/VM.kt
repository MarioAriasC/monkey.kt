@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.vm

import org.marioarias.monkey.code.*
import org.marioarias.monkey.code.OpAdd
import org.marioarias.monkey.code.OpDiv
import org.marioarias.monkey.code.OpEqual
import org.marioarias.monkey.code.OpGreaterThan
import org.marioarias.monkey.code.OpMul
import org.marioarias.monkey.code.OpNotEqual
import org.marioarias.monkey.code.OpSub
import org.marioarias.monkey.compiler.Bytecode
import org.marioarias.monkey.objects.*
import org.marioarias.monkey.objects.HashKey
import org.marioarias.monkey.objects.HashPair
import org.marioarias.monkey.objects.Hashable
import org.marioarias.monkey.objects.MArray
import org.marioarias.monkey.objects.MBuiltinFunction
import org.marioarias.monkey.objects.MClosure
import org.marioarias.monkey.objects.MCompiledFunction
import org.marioarias.monkey.objects.MHash
import org.marioarias.monkey.objects.MInteger
import org.marioarias.monkey.objects.MObject
import org.marioarias.monkey.objects.MString
import org.marioarias.monkey.objects.typeDesc

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

                OpAdd, OpSub, OpMul, OpDiv -> {
                    val right = pop()
                    val left1 = pop()
                    when {
                        left1 is MInteger && right is MInteger -> {
                            val result = when (op) {
                                OpAdd -> left1 + right
                                OpSub -> left1 - right
                                OpMul -> left1 * right
                                OpDiv -> left1 / right
                                else -> throw VMException("unknown integer operator $op")
                            }
                            push(result)
                        }

                        left1 is MString && right is MString -> {
                            when (op) {
                                OpAdd -> push(MString(left1.value + right.value))
                                else -> throw VMException("unknown string operator: $op")
                            }
                        }

                        else -> throw VMException("unsupported types for binary operation: ${left1.typeDesc()} ${right.typeDesc()}")
                    }
                }

                OpTrue -> push(True)
                OpFalse -> push(False)
                OpPop -> pop()
                OpEqual, OpNotEqual, OpGreaterThan -> {
                    val right1 = pop()
                    val left1 = pop()
                    when {
                        left1 is MInteger && right1 is MInteger -> {
                            val leftValue = left1.value
                            val rightValue = right1.value
                            val bool = when (op) {
                                OpEqual -> (leftValue == rightValue).toMBoolean()
                                OpNotEqual -> (leftValue != rightValue).toMBoolean()
                                OpGreaterThan -> (leftValue > rightValue).toMBoolean()
                                else -> throw VMException("unknown operator $op")
                            }
                            push(bool)
                        }

                        else -> when (op) {
                            OpEqual -> push((left1 == right1).toMBoolean())
                            OpNotEqual -> push((left1 != right1).toMBoolean())
                            else -> throw VMException("unknown operator $op (${left1.typeDesc()} ${right1.typeDesc()})")
                        }
                    }
                }

                OpBang -> when (pop()) {
                    True -> push(False)
                    False -> push(True)
                    Null -> push(True)
                    else -> push(False)
                }

                OpMinus -> {
                    val operand = pop()
                    if (operand !is MInteger) {
                        throw VMException("unsupported type for negation: ${operand.typeDesc()}")
                    }
                    push(-operand)
                }

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

                OpArray -> buildAndPush(ins, ip) { startIndex, endIndex ->
                    val elements = Array<MObject?>(endIndex - startIndex) { null }.toMutableList()
                    var i = startIndex
                    while (i < endIndex) {
                        elements[i - startIndex] = stack[i]
                        i++
                    }
                    MArray(elements)
                }

                OpHash -> buildAndPush(ins, ip) { startIndex, endIndex ->
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
                    MHash(hashedPairs)
                }

                OpIndex -> {
                    val index = pop()
                    val left = pop()
                    when {
                        left!! is MArray && index!! is MInteger -> {
                            val i = index.value
                            val max = left.elements.size - 1L
                            if (i < 0 || i > max) {
                                push(Null)
                            } else {
                                push(left.elements[i.toInt()]!!)
                            }
                        }

                        left is MHash -> {
                            when (index!!) {
                                is Hashable<*> -> {
                                    when (val pair = left.pairs[index.hashKey()]) {
                                        null -> push(Null)
                                        else -> push(pair.value)
                                    }
                                }

                                else -> throw VMException("unusable as hash key: ${index.typeDesc()}")
                            }
                        }

                        else -> throw VMException("index operator not supported ${left.typeDesc()}")
                    }
                }

                OpCall -> {
                    val numArgs = ins.readByte(ip + 1)
                    currentFrame().ip++
                    val numArgsInt = numArgs.toInt()
                    when (val callee = stack[sp - 1 - numArgsInt]) {
                        is MClosure -> {
                            if (callee.fn.numParameters != numArgsInt) {
                                throw VMException("wrong number of arguments: want=${callee.fn.numParameters}, got=${numArgsInt}")
                            }
                            val frame = Frame(callee, sp - numArgsInt)
                            pushFrame(frame)
                            sp = frame.basePointer + callee.fn.numLocals
                        }

                        is MBuiltinFunction -> {
                            val args = stack.slice(sp - numArgsInt until sp)
                            val result = callee.fn(args)
                            sp = sp - numArgsInt - 1
                            if (result != null) {
                                push(result)
                            } else {
                                push(Null)
                            }
                        }

                        else -> throw VMException("calling non-function or non-built-in")
                    }
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
                    val numFreeInt = numFree.toInt()
                    when (val constant = constants[constIndex]) {
                        is MCompiledFunction -> {
                            val free = Array(numFreeInt) { i ->
                                stack[sp - numFreeInt - i]!!
                            }.toList()
                            sp -= numFreeInt
                            val closure = MClosure(constant, free)
                            push(closure)
                        }

                        else -> throw VMException("not a function $constant")
                    }
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


    private fun Boolean.toMBoolean() = if (this) {
        True
    } else {
        False
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