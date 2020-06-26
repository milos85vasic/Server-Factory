package net.milosvasic.factory.component.docker

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.obtain.Obtain
import net.milosvasic.factory.configuration.ConfigurationManager
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.Node
import java.io.File

enum class DockerCommand : Obtain<String> {

    DOCKER {
        override fun obtain() = "docker"
    },
    RUN {
        override fun obtain() = "run"
    },
    PS {
        override fun obtain() = "ps"
    },
    BUILD {
        override fun obtain() = "build"
    },
    UP {
        override fun obtain() = "up"
    },
    DOWN {
        override fun obtain() = "down"
    },
    COMMIT {
        override fun obtain() = "commit"
    },
    NETWORK {
        override fun obtain() = "network"
    },
    CREATE {
        override fun obtain() = "create"
    },
    NETWORK_CREATE {

        override fun obtain() = "${DOCKER.obtain()} ${NETWORK.obtain()} ${CREATE.obtain()}"
    },
    COMPOSE {

        @Throws(IllegalStateException::class)
        override fun obtain(): String {

            val context = Context.Docker.context
            val dockerComposePath = Key.DockerComposePath.key
            val configuration = ConfigurationManager.getConfiguration()
            val key = "$context${Node.contextSeparator}$dockerComposePath"
            val variable = configuration.getVariableParsed(key)
            return if (variable != null && variable != String.EMPTY) {
                "$variable${File.separator}docker-compose"
            } else {
                "docker-compose"
            }
        }
    },
    START {
        override fun obtain() = "start"
    },
    STOP {
        override fun obtain() = "stop"
    },
    KILL {
        override fun obtain() = "kill"
    },
    VERSION {
        override fun obtain() = "--version"
    }
}