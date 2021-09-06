package org.marioarias.monkey.compiler

enum class SymbolScope {
    GLOBAL, LOCAL
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

class SymbolTable(private val store: MutableMap<String, Symbol> = mutableMapOf(), val outer: SymbolTable? = null) {
    var numDefinitions: Int = 0

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

    @Throws(SymbolException::class)
    fun resolve(name: String): Symbol {
        return store[name]
            ?: (outer?.resolve(name) ?: throw SymbolException("undefined variable $name"))
    }
}

class SymbolException(message: String) : Exception(message)