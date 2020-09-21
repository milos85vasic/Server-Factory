package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.log
import net.milosvasic.factory.remote.Remote
import java.io.File
import java.nio.file.InvalidPathException
import java.util.concurrent.LinkedBlockingQueue

abstract class Configuration(

        val name: String = String.EMPTY,
        val remote: Remote,
        includes: LinkedBlockingQueue<String>?,
        software: LinkedBlockingQueue<String>?,
        containers: LinkedBlockingQueue<String>?,
        variables: Node? = null,
        enabled: Boolean? = null

) : ConfigurationInclude(

        includes,
        software,
        containers,
        variables,
        enabled
) {

    companion object {

        @Throws(InvalidPathException::class)
        fun getConfigurationFilePath(path: String): String {

            var fullPath = path
            val defaultConfigurationFile = "Definition.json"
            if (!path.endsWith(".json")) {

                val param = FilePathBuilder()
                        .addContext(File.separator)
                        .addContext(defaultConfigurationFile)
                        .build()

                fullPath += param
            }
            return fullPath
        }
    }

    open fun merge(configuration: Configuration) {

        configuration.enabled?.let { enabled ->
            if (enabled) {

                configuration.includes?.let {
                    includes?.addAll(it)
                }
                configuration.variables?.let {
                    variables?.append(it)
                }
                configuration.software?.let {
                    software?.addAll(it)
                }
                configuration.containers?.let {
                    containers?.addAll(it)
                }
            }
        }
    }

    fun mergeVariables(variables: Node?) {
        variables?.let { toAppend ->
            if (this.variables == null) {
                this.variables = toAppend
            } else {
                toAppend.children.forEach { child ->
                    this.variables?.append(child)
                }
            }
        }
    }

    override fun toString(): String {
        return "Configuration(\nname='$name', \nremote=$remote\n)\n${super.toString()}"
    }
}