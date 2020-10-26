package net.milosvasic.factory.platform

enum class Platform(val platformName: String, private val fallback: List<Platform> = listOf()) {

    DOCKER("Docker"),
    CENTOS("CentOS"),
    CENTOS_7("CentOS_7", fallback = listOf(CENTOS)),
    CENTOS_8(CENTOS.platformName),

    UBUNTU("Ubuntu"),
    UBUNTU_18("Ubuntu_18", fallback = listOf(UBUNTU)),
    UBUNTU_19("Ubuntu_19", fallback = listOf(UBUNTU_18, UBUNTU)),
    UBUNTU_20(UBUNTU.platformName),

    UBUNTU_SERVER("Ubuntu_Server", fallback = listOf(UBUNTU)),
    DEBIAN("Debian"),
    FEDORA("Fedora"),
    FEDORA_SERVER("Fedora_Server"),
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