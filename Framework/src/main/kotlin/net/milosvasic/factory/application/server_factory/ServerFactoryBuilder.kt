package net.milosvasic.factory.application.server_factory

import net.milosvasic.factory.configuration.recipe.ConfigurationRecipe
import java.util.logging.Logger
import kotlin.jvm.Throws

class ServerFactoryBuilder {

    private var logger: Logger? = null
    private var recipe: ConfigurationRecipe<*>? = null

    @Throws(IllegalArgumentException::class)
    fun getRecipe(): ConfigurationRecipe<*> {

        recipe?.let {

            return it
        }
        throw IllegalArgumentException("Configuration recipe is not provided")
    }

    fun setRecipe(recipe: ConfigurationRecipe<*>): ServerFactoryBuilder {

        this.recipe = recipe
        return this
    }

    fun getLogger() = logger

    fun setLogger(logger: Logger): ServerFactoryBuilder {

        this.logger = logger
        return this
    }
}