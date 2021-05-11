package org.marioarias.monkey

import kotlin.test.fail

inline fun <reified T> checkType(value: Any?, body: (T) -> Unit) {
    when (value) {
        is T -> {
            body(value)
        }
        else -> {
            fail("$value is not ${T::class.java}. got=${value!!::class.java}")
        }
    }
}

inline fun <reified T> isType(value: Any?, body: (T) -> Boolean): Boolean {
    return when (value) {
        is T -> {
            body(value)
        }
        else -> {
            fail("$value is not ${T::class.java}. got=${value!!::class.java}")
        }
    }
}