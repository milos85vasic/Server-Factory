package net.milosvasic.factory.configuration

import net.milosvasic.factory.configuration.variable.Node
import java.util.concurrent.LinkedBlockingQueue

open class ConfigurationInclude(

        var includes: LinkedBlockingQueue<String>?,
        var software: LinkedBlockingQueue<String>?,
        var containers: LinkedBlockingQueue<String>?,
        var variables: Node? = null
) {

    override fun toString(): String {

        return "ConfigurationInclude(\nincludes=$includes, \nvariables=$variables, \nsoftware=${getSoftwareDefinitions()}, \ncontainers=${getContainersDefinitions()}\n)"
    }

    open fun getSoftwareDefinitions() = software

    open fun getContainersDefinitions() = containers
}