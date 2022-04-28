@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.compiler

import org.marioarias.monkey.ast.*
import org.marioarias.monkey.code.*
import org.marioarias.monkey.objects.*

data class EmittedInstruction(var op: Opcode = 0u, val position: Int = 0)

data class CompilationScope(
    var instructions: Instructions = ubyteArrayOf(),
    var lastInstruction: EmittedInstruction = EmittedInstruction(),
    var previousInstruction: EmittedInstruction = EmittedInstruction()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompilationScope) return false

        if (!instructions.contentEquals(other.instructions)) return false
        if (lastInstruction != other.lastInstruction) return false
        if (previousInstruction != other.previousInstruction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instructions.contentHashCode()
        result = 31 * result + lastInstruction.hashCode()
        result = 31 * result + previousInstruction.hashCode()
        return result
    }
}

class MCompiler(
    private val constants: MutableList<MObject> = mutableListOf(),
    var symbolTable: SymbolTable = SymbolTable()
) {
    private val scopes = mutableListOf(CompilationScope())
    var scopeIndex = 0


    init {
        builtins.forEachIndexed { i, (name, _) ->
            symbolTable.defineBuiltin(i, name)
        }
    }


    @Throws(MCompilerException::class)
    fun compile(node: Node) {
//        println("node = ${node::class.simpleName}")
//        println(currentInstructions())
        when (node) {
            is Program -> node.statements.forEach(this::compile)
            is ExpressionStatement -> {
                compile(node.expression!!)
                emit(OpPop)
            }
            is InfixExpression -> {
                if (node.operator == "<") {
                    compile(node.right!!)
                    compile(node.left!!)
                    emit(OpGreaterThan)
                    return
                }
                compile(node.left!!)
                compile(node.right!!)
                when (node.operator) {
                    "+" -> emit(OpAdd)
                    "-" -> emit(OpSub)
                    "*" -> emit(OpMul)
                    "/" -> emit(OpDiv)
                    ">" -> emit(OpGreaterThan)
                    "==" -> emit(OpEqual)
                    "!=" -> emit(OpNotEqual)
                    else -> throw MCompilerException("unknown operator ${node.operator}")
                }
            }
            is PrefixExpression -> {
                compile(node.right!!)
                when (node.operator) {
                    "!" -> emit(OpBang)
                    "-" -> emit(OpMinus)
                    else -> throw MCompilerException("unknown operator ${node.operator}")
                }
            }
            is IntegerLiteral -> emit(OpConstant, addConstant(MInteger(node.value)))
            is BooleanLiteral -> if (node.value) emit(OpTrue) else emit(OpFalse)
            is IfExpression -> {
                compile(node.condition!!)
                val jumpNotTruthyPos = emit(OpJumpNotTruthy, 9999)
                compile(node.consequence!!)
                if (isLastInstructionPop()) {
                    removeLastPop()
                }
                val jumpPos = emit(OpJump, 9999)

                val afterConsequencePos = currentInstructions().size
                changeOperand(jumpNotTruthyPos, afterConsequencePos)
                if (node.alternative == null) {
                    emit(OpNull)
                } else {
                    compile(node.alternative)
                    if (isLastInstructionPop()) {
                        removeLastPop()
                    }
                }
                val afterAlternativePos = currentInstructions().size
                changeOperand(jumpPos, afterAlternativePos)
            }
            is BlockStatement -> node.statements!!.forEach { statement ->
                compile(statement!!)
            }
            is LetStatement -> {
                val symbol = symbolTable.define(node.name.value)
                compile(node.value!!)
                if (symbol.scope == SymbolScope.GLOBAL) {
                    emit(OpSetGlobal, symbol.index)
                } else {
                    emit(OpSetLocal, symbol.index)
                }
            }
            is Identifier -> {
                val symbol = symbolTable.resolve(node.value)
                loadSymbol(symbol)
            }
            is StringLiteral -> {
                val str = MString(node.value)
                emit(OpConstant, addConstant(str))
            }
            is ArrayLiteral -> {
                node.elements!!.forEach { element ->
                    compile(element!!)
                }
                emit(OpArray, node.elements.size)
            }
            is HashLiteral -> {
                val keys = node.pairs.keys.sortedBy { key -> key.toString() }
                keys.forEach { key ->
                    compile(key)
                    compile(node.pairs[key]!!)
                }
                emit(OpHash, node.pairs.size * 2)
            }
            is IndexExpression -> {
                compile(node.left!!)
                compile(node.index!!)
                emit(OpIndex)
            }
            is FunctionLiteral -> {
                enterScope()
                if (node.name.isNotEmpty()) {
                    symbolTable.defineFunctionName(node.name)
                }
                node.parameters?.forEach { parameter ->
                    symbolTable.define(parameter.value)
                }
                compile(node.body!!)
                if (isLastInstructionPop()) {
                    replaceLastPopWithReturn()
                }
                if (!lastInstructionIs(OpReturnValue)) {
                    emit(OpReturn)
                }

                val freeSymbols = symbolTable.freeSymbols

                val numLocals = symbolTable.numDefinitions
                val instructions = leaveScope()
                
                freeSymbols.forEach(this::loadSymbol)

                val compiledFn = MCompiledFunction(
                    instructions = instructions,
                    numLocals = numLocals,
                    numParameters = node.parameters!!.size
                )
                emit(OpClosure, addConstant(compiledFn), freeSymbols.size)
            }
            is ReturnStatement -> {
                compile(node.returnValue!!)
                emit(OpReturnValue)
            }
            is CallExpression -> {
                compile(node.function!!)
                node.arguments!!.forEach { arg ->
                    compile(arg!!)
                }
                emit(OpCall, node.arguments.size)
            }
        }
    }

    private fun loadSymbol(symbol: Symbol) {
        val opcode = when (symbol.scope) {
            SymbolScope.GLOBAL -> OpGetGlobal
            SymbolScope.LOCAL -> OpGetLocal
            SymbolScope.BUILTIN -> OpGetBuiltin
            SymbolScope.FREE -> OpGetFree
            SymbolScope.FUNCTION -> OpCurrentClosure
        }
        if (opcode != OpCurrentClosure) {
            emit(opcode, symbol.index)
        } else {
            emit(opcode)
        }
    }

    fun leaveScope(): Instructions {
        val instructions = currentInstructions()
        scopes.removeLast()
        scopeIndex--
        symbolTable = symbolTable.outer!!
        return instructions
    }

    private fun replaceLastPopWithReturn() {
        val lasPos = currentScope().lastInstruction.position
        replaceInstruction(lasPos, make(OpReturnValue))
        currentScope().lastInstruction.op = OpReturnValue
    }

    fun enterScope() {
        scopes += CompilationScope()
        symbolTable = SymbolTable(outer = symbolTable)
        scopeIndex++
    }

    private fun changeOperand(opPos: Int, operand: Int) {
        val op = currentInstructions()[opPos]
        val newInstruction = make(op, operand)
        replaceInstruction(opPos, newInstruction)
    }

    private fun replaceInstruction(pos: Int, newInstruction: Instructions) {
        for (i in newInstruction.indices) {
            currentInstructions()[pos + i] = newInstruction[i]
        }
    }

    private fun removeLastPop() {

        with(currentScope()) {
            val last = lastInstruction
            val previous = previousInstruction

            val old = currentInstructions()
            val newInstruction = old.onset(last.position)
            instructions = newInstruction
            lastInstruction = previous
        }

    }

    private fun isLastInstructionPop(): Boolean {
        return lastInstructionIs(OpPop)
    }

    private fun lastInstructionIs(op: Opcode) = currentScope().lastInstruction.op == op

    private fun addConstant(obj: MObject): Int {
        constants += obj
        return constants.size - 1
    }

    private fun addInstruction(ins: Instructions): Int {
        val posNewInstruction = currentInstructions().size
        currentScope().instructions += ins
        return posNewInstruction
    }

    fun emit(op: Opcode, vararg operands: Int): Int {
        val ins = make(op, *operands)
        val pos = addInstruction(ins)
        setLastInstruction(op, pos)
        return pos
    }

    private fun setLastInstruction(op: Opcode, position: Int) {
        with(currentScope()) {
            val previous = lastInstruction
            val last = EmittedInstruction(op, position)
            previousInstruction = previous
            lastInstruction = last
        }
    }

    fun bytecode(): Bytecode = Bytecode(currentInstructions(), constants)

    private fun currentInstructions(): Instructions = currentScope().instructions

    fun currentScope(): CompilationScope = scopes[scopeIndex]
}

data class Bytecode(val instructions: Instructions, val constants: List<MObject>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bytecode) return false

        if (!instructions.contentEquals(other.instructions)) return false
        if (constants != other.constants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instructions.contentHashCode()
        result = 31 * result + constants.hashCode()
        return result
    }
}

class MCompilerException(message: String) : Exception(message)