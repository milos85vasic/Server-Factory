package net.milosvasic.factory.application.server_factory

import net.milosvasic.factory.configuration.ConfigurationFactory

class CommonServerFactory(builder: ServerFactoryBuilder) : ServerFactory(builder) {

    override fun getConfigurationFactory(): ConfigurationFactory<*> {

        TODO("Not yet implemented")
    }
}