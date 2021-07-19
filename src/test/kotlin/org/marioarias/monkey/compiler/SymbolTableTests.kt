package org.marioarias.monkey.compiler

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class SymbolTableTests {
    @Test
    fun define() {

        fun testSymbol(name: String, global: SymbolTable, expected: Map<String, Symbol>) {
            val symbol = global.define(name)
            val expectedSymbol = expected[name]
            assertEquals(expectedSymbol, symbol)
        }

        val expected = mapOf(
            "a" to Symbol("a", SymbolScope.GLOBAL, 0),
            "b" to Symbol("b", SymbolScope.GLOBAL, 1)
        )

        val global = SymbolTable()

        testSymbol("a", global, expected)
        testSymbol("b", global, expected)
    }

    @Test
    fun `resolve global`() {
        val global = SymbolTable()
        global.define("a")
        global.define("b")

        val expected = mapOf(
            "a" to Symbol("a", SymbolScope.GLOBAL, 0),
            "b" to Symbol("b", SymbolScope.GLOBAL, 1)
        )

        expected.values.forEach { symbol ->
            try {
                val result = global.resolve(symbol.name)
                assertEquals(symbol, result)
            } catch (e: SymbolException) {
                fail("name ${symbol.name} not resolvable")
            }
        }
    }


}