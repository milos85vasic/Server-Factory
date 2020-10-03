package net.milosvasic.factory.configuration

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.common.obtain.ObtainParametrized
import net.milosvasic.factory.component.installer.step.InstallationStep
import net.milosvasic.factory.component.installer.step.factory.InstallationStepFactories
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.log
import net.milosvasic.factory.merge
import net.milosvasic.factory.os.OSType
import net.milosvasic.factory.os.OperatingSystem
import net.milosvasic.factory.validation.Validator
import java.io.File

data class SoftwareConfiguration(

        var operatingSystem: OperatingSystem?,
        var overrides: MutableMap<String, MutableMap<String, SoftwareConfiguration>>? = mutableMapOf(),
        var configuration: String = String.EMPTY,
        var variables: Node? = null,
        var software: MutableList<SoftwareConfigurationItem>? = mutableListOf(),
        var includes: MutableList<String>? = mutableListOf(),
        var enabled: Boolean? = true

) : ObtainParametrized<String, Map<String, List<InstallationStep<*>>>> {

    companion object : ObtainParametrized<String, SoftwareConfiguration> {

        @Throws(IllegalArgumentException::class, JsonParseException::class)
        override fun obtain(vararg param: String): SoftwareConfiguration {

            Validator.Arguments.validateSingle(param)
            val configurationName = param[0]
            val configurationFile = File(configurationName)
            log.d("Configuration file: ${configurationFile.absolutePath}")
            if (configurationFile.exists()) {

                val json = configurationFile.readText()
                val gsonBuilder = GsonBuilder()
                val variablesDeserializer = Node.getDeserializer()
                gsonBuilder.registerTypeAdapter(Node::class.java, variablesDeserializer)
                val gson = gsonBuilder.create()

                val instance = gson.fromJson(json, SoftwareConfiguration::class.java)
                instance.configuration = configurationName
                val included = mutableListOf<SoftwareConfiguration>()
                instance.includes?.forEach { include ->

                    var path = include
                    if (!include.startsWith(File.separator)) {

                        path = FilePathBuilder()
                                .addContext(configurationFile.parent)
                                .addContext(include)
                                .getPath()
                    }
                    included.add(obtain(path))
                }
                included.forEach { config ->
                    instance.merge(config)
                }
                // TODO: Handle vars.
                return instance
            } else {

                val msg = "Software configuration file does not exist: ${configurationFile.absolutePath}"
                throw IllegalArgumentException(msg)
            }
        }
    }

    @Synchronized
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun obtain(vararg param: String): Map<String, List<InstallationStep<*>>> {

        Validator.Arguments.validateSingle(param)
        val os = param[0]
        val factories = InstallationStepFactories
        val installationSteps = mutableMapOf<String, List<InstallationStep<*>>>()
        software?.forEach {
            val steps = it.installationSteps[os]
            steps?.let { recipe ->
                val items = mutableListOf<InstallationStep<*>>()
                recipe.forEach { definition ->
                    items.add(factories.obtain(definition))
                }
                installationSteps[it.name] = items
            }
        }
        if (installationSteps.isEmpty()) {
            throw IllegalArgumentException("No installation steps for '$os' platform")
        }
        return installationSteps
    }

    @Throws(IllegalArgumentException::class)
    fun merge(configuration: SoftwareConfiguration) {

        if (operatingSystem == null) {

            throw IllegalArgumentException("No operating system information provided for remote host")
        }
        if (operatingSystem?.getType() == OSType.UNKNOWN) {

            throw IllegalArgumentException("Operating system information provided for remote host is unknown")
        }

        merge(configuration.variables)

        configuration.software?.let {

            if (software == null) {
                software = mutableListOf()
            }
            software?.addAll(it)
        }

        configuration.includes?.let {

            if (includes == null) {
                includes = mutableListOf()
            }
            includes?.addAll(it)
        }

        configuration.overrides?.let {

            if (overrides == null) {
                overrides = mutableMapOf()
            }
            overrides?.merge(it)
        }

        overrides?.let {
            operatingSystem?.let { os ->
                it[SoftwareConfigurationOverride.OS.type]?.let { osOverrides ->

                    val cfg = osOverrides[os.getName()]
                    cfg?.let {
                        merge(it)
                    }
                }
            }
        }
    }

    fun isEnabled(): Boolean {

        enabled?.let {
            return it
        }
        return true
    }

    private fun merge(toMerge: Node?) {

        toMerge?.let { toAppend ->
            if (variables == null) {
                variables = toAppend
            } else {
                variables?.append(toAppend)
            }
        }
    }
}