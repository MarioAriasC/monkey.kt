package org.marioarias.monkey.objects

import org.marioarias.monkey.ast.BlockStatement
import org.marioarias.monkey.ast.Identifier
import org.marioarias.monkey.evaluator.Environment

enum class ObjectType {
    INTEGER, BOOLEAN, NULL, RETURN, ERROR, FUNCTION
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

class MInteger(override val value: Long) : MValue<Long> {
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
}

class MBoolean(override val value: Boolean) : MValue<Boolean> {
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