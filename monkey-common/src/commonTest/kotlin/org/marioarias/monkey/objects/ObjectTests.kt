package org.marioarias.monkey.objects

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class ObjectTests {
    @Test
    fun `string hash key`() {
        val hello1 = MString("Hello World")
        val hello2 = MString("Hello World")
        val diff1 = MString("My name is johnny")
        val diff2 = MString("My name is johnny")

        assertEquals(hello1.hashKey(), hello2.hashKey(), "string with same content have different hash keys")
        assertEquals(diff1.hashKey(), diff2.hashKey(), "string with same content have different hash keys")
        assertNotEquals(hello1.hashKey(), diff2.hashKey(), "string with different content have same hash keys")

    }
}