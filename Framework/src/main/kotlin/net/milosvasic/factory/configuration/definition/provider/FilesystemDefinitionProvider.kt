package net.milosvasic.factory.configuration.definition.provider

import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.log

class FilesystemDefinitionProvider : DefinitionProvider {

    override fun load(definition: Definition): Boolean {

        val home = definition.getHome()
        if (home.exists()) {

            log.w("Definition found: $definition")
        } else {

            log.w("Definition not found: $definition")
        }
        return false
    }
}