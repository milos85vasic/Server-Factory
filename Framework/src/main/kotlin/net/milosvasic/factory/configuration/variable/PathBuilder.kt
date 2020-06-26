package net.milosvasic.factory.configuration.variable

import net.milosvasic.factory.common.Build

class PathBuilder : Build<String> {

    private var key: Key? = null
    private val builder = StringBuilder()
    private val contexts = mutableListOf<Context>()

    fun addContext(context: Context): PathBuilder {
        contexts.add(context)
        return this
    }

    fun setKey(key: Key): PathBuilder {
        this.key = key
        return this
    }

    fun hasContexts() = contexts.isNotEmpty()

    fun hasKey() = key != null

    @Throws(IllegalArgumentException::class)
    override fun build(): String {

        val validator = PathBuilderValidator()
        if (validator.validate(this)) {
            contexts.forEach { context ->
                builder
                        .append(context.context)
                        .append(Node.contextSeparator)
            }
            key?.let {
                builder.append(it.key)
            }
            return builder.toString()
        } else {

            throw IllegalArgumentException("Cannot build path")
        }
    }
}