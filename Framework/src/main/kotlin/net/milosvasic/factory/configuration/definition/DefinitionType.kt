package net.milosvasic.factory.configuration.definition

enum class DefinitionType(val type: String) {

    Stack("stack"),
    Software("software"),
    Docker("docker"),
    Unknown("unknown")
}