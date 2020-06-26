package net.milosvasic.factory.configuration.variable

class Path(private val builder: PathBuilder) {

    fun getPath() = builder.build()

    override fun toString(): String {
        return "Path(path=${getPath()})"
    }
}