package net.milosvasic.factory.application

interface BuildInformation {

    val versionName: String
    val version: String
    val versionCode: Int

    fun printName(): String
}