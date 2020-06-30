package net.milosvasic.factory.configuration.variable

import net.milosvasic.factory.component.docker.DockerCommand

enum class Context(val context: String) {

    Server("SERVER"),
    Ports("PORTS"),
    Service("SERVICE"),
    Database("DATABASE"),
    Certification("CERTIFICATION"),
    Docker(DockerCommand.DOCKER.obtain().toUpperCase())
}