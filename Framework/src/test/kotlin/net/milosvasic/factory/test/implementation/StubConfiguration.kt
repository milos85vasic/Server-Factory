package net.milosvasic.factory.test.implementation

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.configuration.Configuration
import net.milosvasic.factory.configuration.SoftwareConfiguration
import net.milosvasic.factory.configuration.definition.Definition
import net.milosvasic.factory.configuration.variable.Node
import net.milosvasic.factory.remote.Remote
import java.util.concurrent.LinkedBlockingQueue

class StubConfiguration(

        name: String = String.EMPTY,
        remote: Remote,
        uses: LinkedBlockingQueue<Definition>?,
        includes: LinkedBlockingQueue<String>?,
        software: LinkedBlockingQueue<String>,
        containers: LinkedBlockingQueue<String>?,
        variables: Node? = null,
        overrides: MutableMap<String, MutableMap<String, SoftwareConfiguration>>?

) : Configuration(name, remote, uses, includes, software, containers, variables, overrides)