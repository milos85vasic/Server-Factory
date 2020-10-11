package net.milosvasic.factory.configuration.definition

enum class DefinitionType(val type: String) {

    Stacks("stacks"),
    Docker("docker"),
    Unknown("unknown"),
    Software("software")
}