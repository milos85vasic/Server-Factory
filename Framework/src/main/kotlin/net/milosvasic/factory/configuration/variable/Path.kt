package net.milosvasic.factory.configuration.variable

class Path(private val builder: PathBuilder) {

    fun getPath() = builder.getPath()

    override fun toString(): String {
        return "Path(path=${getPath()})"
    }
}