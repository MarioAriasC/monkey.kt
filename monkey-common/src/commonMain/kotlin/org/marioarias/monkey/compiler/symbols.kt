package org.marioarias.monkey.compiler

enum class SymbolScope {
    GLOBAL, LOCAL, BUILTIN, FREE, FUNCTION
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

class SymbolTable(private val store: MutableMap<String, Symbol> = mutableMapOf(), val outer: SymbolTable? = null) {
    var numDefinitions: Int = 0
    val freeSymbols = mutableListOf<Symbol>()

    fun define(name: String): Symbol {
        val scope = if (outer == null) {
            SymbolScope.GLOBAL
        } else {
            SymbolScope.LOCAL
        }
        val symbol = Symbol(name, scope, numDefinitions)
        store[name] = symbol
        numDefinitions++
        return symbol
    }

    fun defineBuiltin(index: Int, name: String): Symbol {
        val stored = store[name]
        return if (stored == null) {
            val symbol = Symbol(name, SymbolScope.BUILTIN, index)
            store[name] = symbol
            symbol
        } else {
            stored
        }
    }

    @Throws(SymbolException::class)
    fun resolve(name: String): Symbol {
        return store[name]
            ?: if (outer != null) {
                val symbol = outer.resolve(name)
                if (symbol.scope == SymbolScope.GLOBAL || symbol.scope == SymbolScope.BUILTIN) {
                    symbol
                } else {
                    defineFree(symbol)
                }
            } else {
                throw SymbolException("undefined variable $name")
            }
    }

    fun defineFunctionName(name: String): Symbol {
        val symbol = Symbol(name, SymbolScope.FUNCTION, 0)
        store[name] = symbol
        return symbol
    }

    private fun defineFree(original: Symbol): Symbol {
        freeSymbols.add(original)
        val symbol = Symbol(original.name, SymbolScope.FREE, freeSymbols.size - 1)
        store[original.name] = symbol
        return symbol
    }
}

class SymbolException(message: String) : Exception(message)