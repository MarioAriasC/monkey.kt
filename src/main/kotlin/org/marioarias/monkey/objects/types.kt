package org.marioarias.monkey.objects

import org.marioarias.monkey.ast.BlockStatement
import org.marioarias.monkey.ast.Identifier
import org.marioarias.monkey.ast.Node
import org.marioarias.monkey.evaluator.Environment

enum class ObjectType {
    INTEGER, BOOLEAN, NULL, RETURN, ERROR, FUNCTION, STRING, BUILTIN, ARRAY, HASH, QUOTE, MACRO
}

interface MObject {
    fun type(): ObjectType
    fun inspect(): String
}

interface MValue<T> : MObject {
    val value: T

    override fun inspect(): String {
        return value.toString()
    }
}

class MInteger(override val value: Long) : MValue<Long>, Hashable<Long> {
    override fun type(): ObjectType {
        return ObjectType.INTEGER
    }

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


}

class MBoolean(override val value: Boolean) : MValue<Boolean>, Hashable<Boolean> {
    override fun type(): ObjectType {
        return ObjectType.BOOLEAN
    }
}

class MReturnValue(val value: MObject) : MObject {
    override fun type(): ObjectType {
        return ObjectType.RETURN
    }

    override fun inspect(): String {
        return value.inspect()
    }
}

class MError(val message: String) : MObject {
    override fun type(): ObjectType {
        return ObjectType.ERROR
    }

    override fun inspect(): String {
        return "ERROR: $message"
    }

    override fun toString(): String {
        return "MError(message='$message')"
    }
}

object MNull : MObject {
    override fun type(): ObjectType {
        return ObjectType.NULL
    }

    override fun inspect(): String {
        return "null"
    }
}

class MFunction(val parameters: List<Identifier>?, val body: BlockStatement?, val env: Environment) : MObject {
    override fun type(): ObjectType {
        return ObjectType.FUNCTION
    }

    override fun inspect(): String {
        return "fn(${parameters?.joinToString(transform = Identifier::toString) ?: ""}) {\n\t$body\n}"
    }

}

class MString(override val value: String) : MValue<String>, Hashable<String> {
    override fun type(): ObjectType = ObjectType.STRING

    operator fun plus(other: MString): MString {
        return MString(value + other.value)
    }
}

typealias BuiltinFunction = (List<MObject?>) -> MObject

class MBuiltinFunction(val fn: BuiltinFunction) : MObject {
    override fun type(): ObjectType = ObjectType.BUILTIN

    override fun inspect(): String = "builtin function"
}

class MArray(val elements: List<MObject?>): MObject {
    override fun type(): ObjectType = ObjectType.ARRAY

    override fun inspect(): String {
        return "[${elements.joinToString(separator = ", ")}]"
    }
}

data class HashKey(val type: ObjectType, val value: Int)

interface Hashable<T> : MValue<T>{
    fun hashKey(): HashKey = HashKey(type(), value.hashCode())
    
}

data class HashPair(val key:MObject, val value:MObject)

class MHash(val pairs: Map<HashKey, HashPair>): MObject {
    override fun type(): ObjectType {
        return ObjectType.HASH
    }

    override fun inspect(): String {
        return "{${pairs.values.joinToString { (key, value) -> "${key.inspect()}: ${value.inspect()}" }}}"
    }
}

class MQuote(val node: Node?):MObject {
    override fun type(): ObjectType {
        return ObjectType.QUOTE
    }

    override fun inspect(): String {
        return "QUOTE($node)"
    }
}

class MMacro(val parameters:List<Identifier>?, val body: BlockStatement?, val env: Environment):MObject{
    override fun type(): ObjectType {
        return ObjectType.MACRO
    }

    override fun inspect(): String {
        return "macro(${parameters?.joinToString()}) {\n$body\n}"
    }

}