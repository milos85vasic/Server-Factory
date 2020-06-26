package net.milosvasic.factory.configuration

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.configuration.variable.Variable

class InstallationStepDefinition(
    val type: String,
    val name: String = String.EMPTY,
    private val value: String
) {

    @Throws(IllegalStateException::class)
    fun getValue(): String = Variable.parse(value)
}