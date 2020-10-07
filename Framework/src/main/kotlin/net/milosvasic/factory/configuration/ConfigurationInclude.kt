package net.milosvasic.factory.configuration

import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.configuration.variable.Node
import java.util.concurrent.LinkedBlockingQueue

open class ConfigurationInclude(

        var definition: Definition? = null,
        var uses: LinkedBlockingQueue<String>?,
        var includes: LinkedBlockingQueue<String>?,
        var software: LinkedBlockingQueue<String>?,
        var containers: LinkedBlockingQueue<String>?,
        var variables: Node? = null,
        var overrides: MutableMap<String, MutableMap<String, SoftwareConfiguration>>?,
        var enabled: Boolean? = null
) {

    fun getConfigurationMap() = mapOf(

            SoftwareConfigurationType.SOFTWARE to software,
            SoftwareConfigurationType.STACKS to containers,
            SoftwareConfigurationType.DOCKER to software
    )

    override fun toString(): String {

        return "ConfigurationInclude(definition=$definition, uses=$uses, includes=$includes, software=$software, " +
                "containers=$containers, variables=$variables, overrides=$overrides, enabled=$enabled)"
    }
}