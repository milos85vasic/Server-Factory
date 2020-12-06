package net.milosvasic.factory.operation.command

import net.milosvasic.factory.operation.Operation

open class Command(private val toExecute: String) : Operation() {

    override fun toString() = "${this::class.simpleName}(toExecute='$toExecute')"
}