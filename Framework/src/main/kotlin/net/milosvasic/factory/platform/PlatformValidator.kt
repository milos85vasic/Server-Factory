package net.milosvasic.factory.platform

import net.milosvasic.factory.common.Validation

class PlatformValidator(private val notAllowed: List<Platform> = listOf(Platform.UNKNOWN)) : Validation<Platform> {

    override fun validate(vararg what: Platform): Boolean {

        TODO("Not yet implemented")
    }
}