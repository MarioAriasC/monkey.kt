package org.marioarias.monkey.compiler

enum class SymbolScope {
    GLOBAL
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

class SymbolTable(private val store: MutableMap<String, Symbol> = mutableMapOf()) {
    var numDefinitions: Int = 0

    fun define(name: String): Symbol {
        val symbol = Symbol(name, SymbolScope.GLOBAL, numDefinitions)
        store[name] = symbol
        numDefinitions++
        return symbol
    }

    @Throws(SymbolException::class)
    fun resolve(name: String): Symbol {
        val symbol = store[name]
        if(symbol != null) {
            return symbol
        } else {
            throw SymbolException("undefined variable $name")
        }
    }
}

class SymbolException(message: String): Exception(message)