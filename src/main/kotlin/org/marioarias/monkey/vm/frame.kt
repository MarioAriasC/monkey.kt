package org.marioarias.monkey.vm

import org.marioarias.monkey.code.Instructions
import org.marioarias.monkey.objects.MCompiledFunction

class Frame(private val fn: MCompiledFunction, internal val basePointer: Int) {
    public var ip = -1

    public fun instructions(): Instructions = fn.instructions
}