package net.milosvasic.factory.remote

import com.google.gson.annotations.SerializedName
import net.milosvasic.factory.LOCALHOST

data class Remote(
        val host: String = LOCALHOST,
        val port: Int,
        @SerializedName("user") val account: String
)