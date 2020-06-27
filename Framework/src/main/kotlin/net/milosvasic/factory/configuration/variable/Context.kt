package net.milosvasic.factory.configuration.variable

import net.milosvasic.factory.component.docker.DockerCommand

enum class Context(val context: String) {

    Database("DB"),
    Server("SERVER"),
    Postfix("POSTFIX"),
    Certification("CERTIFICATION"),
    Docker(DockerCommand.DOCKER.obtain().toUpperCase())
}