package org.marioarias.monkey.evaluator

import org.marioarias.monkey.objects.MObject

class Environment private constructor(val store: MutableMap<String, MObject>, val outer: Environment?) {
    companion object {
        fun newEnvironment(): Environment {
            return Environment(mutableMapOf(), null)
        }

        fun newEnclosedEnvironment(outer: Environment): Environment {
            return Environment(mutableMapOf(), outer)
        }
    }

    operator fun set(name: String, value: MObject) {
        store[name] = value
    }

    fun put(name: String, value: MObject): MObject {
        this[name] = value
        return value
    }

    operator fun get(name: String): MObject? {
        val obj = store[name]
        if (obj == null && outer != null) {
            return outer[name]
        }
        return obj
    }
}