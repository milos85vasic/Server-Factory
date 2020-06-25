package net.milosvasic.factory.configuration

import net.milosvasic.factory.component.docker.DockerCommand

enum class VariableContext(val context: String) {

    Database("DB"),
    Server("SERVER"),
    Postfix("POSTFIX"),
    Certification("CERTIFICATION"),
    Docker(DockerCommand.DOCKER.obtain().toUpperCase())
}