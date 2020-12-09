package net.milosvasic.factory.configuration.builder

import net.milosvasic.factory.common.Build
import net.milosvasic.factory.configuration.SoftwareConfigurationItem

class SoftwareBuilder : Build<MutableList<SoftwareConfigurationItem>> {

    private val items = mutableListOf<SoftwareConfigurationItem>()

    @Throws(IllegalArgumentException::class)
    fun addItem(builder: SoftwareConfigurationItemBuilder): SoftwareBuilder {

        items.add(builder.build())
        return this
    }

    @Throws(IllegalArgumentException::class)
    override fun build(): MutableList<SoftwareConfigurationItem> {

        // TODO: Validate
        return items
    }
}