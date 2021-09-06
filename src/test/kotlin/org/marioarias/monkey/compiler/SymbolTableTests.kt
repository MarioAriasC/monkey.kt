package org.marioarias.monkey.compiler


import org.testng.annotations.Test
import kotlin.test.assertEquals
import kotlin.test.fail


class SymbolTableTests {
    @Test
    fun define() {

        val expected = mapOf(
            "a" to Symbol("a", SymbolScope.GLOBAL, 0),
            "b" to Symbol("b", SymbolScope.GLOBAL, 1),
            "c" to Symbol("c", SymbolScope.LOCAL, 0),
            "d" to Symbol("d", SymbolScope.LOCAL, 1),
            "e" to Symbol("e", SymbolScope.LOCAL, 0),
            "f" to Symbol("f", SymbolScope.LOCAL, 1),
        )

        fun testSymbol(name: String, table: SymbolTable) {
            val symbol = table.define(name)
            val expectedSymbol = expected[name]
            assertEquals(expectedSymbol, symbol)
        }

        val global = SymbolTable()

        testSymbol("a", global)
        testSymbol("b", global)

        val firstLocal = SymbolTable(outer = global)

        testSymbol("c", firstLocal)
        testSymbol("d", firstLocal)

        val secondLocal = SymbolTable(outer = global)

        testSymbol("e", secondLocal)
        testSymbol("f", secondLocal)
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
                testSymbol(global, symbol)
            } catch (e: SymbolException) {
                fail("name ${symbol.name} not resolvable")
            }
        }
    }

    @Test
    fun `resolve local`() {
        val global = SymbolTable()
        global.define("a")
        global.define("b")

        val local = SymbolTable(outer = global)
        local.define("c")
        local.define("d")

        listOf(
            Symbol("a", SymbolScope.GLOBAL, 0),
            Symbol("b", SymbolScope.GLOBAL, 1),
            Symbol("c", SymbolScope.LOCAL, 0),
            Symbol("d", SymbolScope.LOCAL, 1),
        ).forEach { sym ->
            testSymbol(local, sym)
        }
    }

    private fun testSymbol(table: SymbolTable, sym: Symbol) {
        val result = table.resolve(sym.name)
        assertEquals(sym, result)
    }

    @Test
    fun `resolved nested local`() {
        val global = SymbolTable()
        global.define("a")
        global.define("b")

        val firstLocal = SymbolTable(outer = global)
        firstLocal.define("c")
        firstLocal.define("d")

        val secondLocal = SymbolTable(outer = global)
        secondLocal.define("e")
        secondLocal.define("f")

        listOf(
            firstLocal to listOf(
                Symbol("a", SymbolScope.GLOBAL, 0),
                Symbol("b", SymbolScope.GLOBAL, 1),
                Symbol("c", SymbolScope.LOCAL, 0),
                Symbol("d", SymbolScope.LOCAL, 1),
            ),
            secondLocal to listOf(
                Symbol("a", SymbolScope.GLOBAL, 0),
                Symbol("b", SymbolScope.GLOBAL, 1),
                Symbol("e", SymbolScope.LOCAL, 0),
                Symbol("f", SymbolScope.LOCAL, 1),
            )
        ).forEach { (table, symbols) ->
            symbols.forEach { symbol ->
                testSymbol(table, symbol)
            }
        }

    }


}