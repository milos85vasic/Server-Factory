package net.milosvasic.factory.remote

import com.google.gson.annotations.SerializedName
import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.LOCALHOST
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable

data class Remote(
        private val host: String?,
        val port: Int,
        @SerializedName("user") val account: String
) {

    fun getHost(): String {

        host?.let { return it }

        val path = PathBuilder()
                .addContext(Context.Server)
                .setKey(Key.Hostname)
                .build()

        val hostname = Variable.get(path)
        if (hostname != String.EMPTY) {

            return hostname
        }
        return LOCALHOST
    }
}