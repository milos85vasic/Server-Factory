package net.milosvasic.factory.platform

enum class Platform(val platformName: String, private val fallback: List<Platform> = listOf()) {

    DOCKER("Docker"),
    CENTOS("CentOS"),
    CENTOS_7("CentOS_7", fallback = listOf(CENTOS)),
    UBUNTU("Ubuntu"),
    UBUNTU_SERVER("Ubuntu_Server", fallback = listOf(UBUNTU)),
    DEBIAN("Debian"),
    FEDORA("Fedora", fallback = listOf(CENTOS)),
    FEDORA_30("Fedora_30", fallback = listOf(FEDORA, CENTOS)),
    FEDORA_31("Fedora_31", fallback = listOf(FEDORA, CENTOS)),
    FEDORA_32("Fedora_31", fallback = listOf(FEDORA_31, FEDORA, CENTOS)),
    FEDORA_33("Fedora_31", fallback = listOf(FEDORA_32, FEDORA_31,FEDORA, CENTOS)),
    FEDORA_SERVER("Fedora_Server", fallback = listOf(FEDORA, CENTOS)),
    FEDORA_SERVER_30("Fedora_Server_30", fallback = listOf(FEDORA_30, FEDORA_SERVER, FEDORA, CENTOS)),
    FEDORA_SERVER_31("Fedora_Server_31", fallback = listOf(FEDORA_SERVER_30, FEDORA_SERVER, FEDORA, CENTOS)),
    FEDORA_SERVER_32("Fedora_Server_32", fallback = listOf(FEDORA_SERVER_31, FEDORA_SERVER_30, FEDORA_SERVER, FEDORA, CENTOS)),
    FEDORA_SERVER_33("Fedora_Server_33", fallback = listOf(FEDORA_SERVER_32, FEDORA_SERVER_31, FEDORA_SERVER_30, FEDORA_SERVER, FEDORA, CENTOS)),
    REDHAT("RedHat"),
    LINUX("Linux"),
    MAC_OS("macOS"),
    WINDOWS("Windows"),
    UNKNOWN("Unknown");

    companion object {

        fun getByValue(value: String) : Platform {

            values().forEach {
                if (value == it.platformName) {
                    return it
                }
            }
            return UNKNOWN
        }
    }

    fun getFallback(): List<Platform> {

        val items = mutableListOf<Platform>()
        items.addAll(fallback)
        items.add(DOCKER)
        return items
    }
}