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
    FEDORA_SERVER("Fedora_Server", fallback = listOf(FEDORA, CENTOS)),
    FEDORA_SERVER_30("Fedora_Server_30", fallback = listOf(FEDORA_30, FEDORA_SERVER, FEDORA, CENTOS)),
    REDHAT("RedHat"),
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