@file:OptIn(ExperimentalUnsignedTypes::class)

package org.marioarias.monkey.vm

import org.marioarias.monkey.code.Instructions
import org.marioarias.monkey.objects.MClosure

class Frame(val cl: MClosure, internal val basePointer: Int) {
    var ip = -1

    fun instructions(): Instructions = cl.fn.instructions
}