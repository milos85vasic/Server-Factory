package net.milosvasic.factory.configuration.variable

import net.milosvasic.factory.common.path.HierarchyPathBuilder

class PathBuilder : HierarchyPathBuilder<Path, Context, Key, String>() {

    private val builder = StringBuilder()

    override val separator: String
        get() = Node.CONTEXT_SEPARATOR

    fun getPath(): String = builder.toString()

    @Throws(IllegalArgumentException::class)
    override fun build(): Path {

        val validator = PathBuilderValidator()
        if (validator.validate(this)) {
            contexts.forEach { context ->
                builder
                        .append(context.context())
                        .append(separator)
            }
            key?.let {
                builder.append(it.key())
            }
            return Path(this)
        } else {

            throw IllegalArgumentException("Cannot build path")
        }
    }
}