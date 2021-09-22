@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.objects

import org.marioarias.monkey.ast.BlockStatement
import org.marioarias.monkey.ast.Identifier
import org.marioarias.monkey.code.Instructions
import org.marioarias.monkey.evaluator.Environment

interface MObject {
    fun inspect(): String

    fun typeDesc(): String
}

fun MObject?.typeDesc(): String {
    return if (this == null) {
        "null"
    } else {
//        this::class.simpleName!!
        this.typeDesc()
    }
}

interface MValue<T> : MObject {
    val value: T

    override fun inspect(): String {
        return value.toString()
    }
}

class MInteger(override val value: Long) : MValue<Long>, Hashable<Long> {

    operator fun compareTo(other: MInteger): Int {
        return value.compareTo(other.value)
    }

    operator fun plus(other: MInteger): MInteger {
        return MInteger(value + other.value)
    }

    operator fun minus(other: MInteger): MInteger {
        return MInteger(value - other.value)
    }

    operator fun times(other: MInteger): MInteger {
        return MInteger(value * other.value)
    }

    operator fun div(other: MInteger): MInteger {
        return MInteger(value / other.value)
    }

    operator fun unaryMinus(): MInteger {
        return MInteger(-value)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MInteger) {
            value == other.value
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "MInteger(value=$value)"
    }

    override fun hashType(): HashType = HashType.INTEGER

    override fun typeDesc(): String = "MInteger"


}

class MBoolean(override val value: Boolean) : MValue<Boolean>, Hashable<Boolean> {
    override fun hashType(): HashType = HashType.BOOLEAN

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MBoolean) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun typeDesc(): String = "MBoolean"
}

class MReturnValue(val value: MObject) : MObject {

    override fun inspect(): String {
        return value.inspect()
    }

    override fun typeDesc(): String = "MReturnValue"
}

class MError(val message: String) : MObject {
    override fun inspect(): String {
        return "ERROR: $message"
    }

    override fun toString(): String {
        return "MError(message='$message')"
    }

    override fun typeDesc(): String = "MError"
}

object MNull : MObject {
    override fun inspect(): String {
        return "null"
    }

    override fun typeDesc(): String = "MNull"
}

class MFunction(val parameters: List<Identifier>?, val body: BlockStatement?, val env: Environment) : MObject {
    override fun inspect(): String {
        return "fn(${parameters?.joinToString(transform = Identifier::toString) ?: ""}) {\n\t$body\n}"
    }

    override fun typeDesc(): String = "MFunction"
}

class MString(override val value: String) : MValue<String>, Hashable<String> {
    operator fun plus(other: MString): MString {
        return MString(value + other.value)
    }

    override fun hashType(): HashType = HashType.STRING

    override fun typeDesc(): String = "MString"
}

typealias BuiltinFunction = (List<MObject?>) -> MObject?

class MBuiltinFunction(val fn: BuiltinFunction) : MObject {
    override fun inspect(): String = "builtin function"

    override fun typeDesc(): String = "MBuiltinFunction"
}

class MArray(val elements: List<MObject?>) : MObject {
    override fun inspect(): String {
        return "[${elements.joinToString(separator = ", ")}]"
    }

    override fun typeDesc(): String = "MArray"
}

enum class HashType {
    INTEGER, BOOLEAN, STRING
}

data class HashKey(val type: HashType, val value: Int)


interface Hashable<T> : MValue<T> {
    fun hashKey(): HashKey = HashKey(hashType(), value.hashCode())

    fun hashType(): HashType
}

data class HashPair(val key: MObject, val value: MObject)

class MHash(val pairs: Map<HashKey, HashPair>) : MObject {

    override fun inspect(): String {
        return "{${pairs.values.joinToString { (key, value) -> "${key.inspect()}: ${value.inspect()}" }}}"
    }

    override fun typeDesc(): String = "MHash"
}

class MCompiledFunction(val instructions: Instructions, val numLocals: Int = 0, val numParameters: Int = 0) : MObject {
    override fun inspect(): String {
        return "CompiledFunction[$this]"
    }

    override fun typeDesc(): String = "MCompiledFunction"
}

class MClosure(val fn: MCompiledFunction, val free: List<MObject> = emptyList()) : MObject {
    override fun inspect(): String {
        return "Closure[$this]"
    }

    override fun typeDesc(): String = "MClosure"
}