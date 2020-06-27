package net.milosvasic.factory.common.path

import net.milosvasic.factory.common.Build

abstract class PathBuilder<T, CTX, SEP>: Build<T> {

    abstract val separator: SEP

    protected val contexts = mutableListOf<CTX>()

    open fun addContext(context: CTX): PathBuilder<T, CTX, SEP> {
        contexts.add(context)
        return this
    }

    fun hasContexts() = contexts.isNotEmpty()
}