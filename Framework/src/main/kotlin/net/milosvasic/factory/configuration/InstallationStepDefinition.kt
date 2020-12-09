package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.component.installer.step.InstallationStepType
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.configuration.variable.Variable

class InstallationStepDefinition constructor(

    val type: String,
    val name: String = String.EMPTY,
    private val value: String
) {

    constructor(

        type: InstallationStepType,
        name: String = String.EMPTY,
        value: String

    ) : this(type.type, name, value)

    private var definition: Definition? = null

    @Throws(IllegalStateException::class)
    fun getValue(): String = Variable.parse(value)

    fun setDefinition(definition: Definition) {

        this.definition = definition
    }

    @Throws(IllegalStateException::class)
    fun getDefinition(): Definition {

        definition?.let {
            return it
        }
        throw IllegalStateException("Parent definition context is not set for $this")
    }
}