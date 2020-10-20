package net.milosvasic.factory.os

enum class OSType(val osName: String, val fallback: List<OSType> = listOf()) {

    CENTOS("CentOS"),
    CENTOS_7("CentOS_7", fallback = listOf(CENTOS)),
    CENTOS_8(CENTOS.osName),

    UBUNTU("Ubuntu"),
    UBUNTU_18("Ubuntu_18", fallback = listOf(UBUNTU)),
    UBUNTU_19("Ubuntu_19", fallback = listOf(UBUNTU_18, UBUNTU)),
    UBUNTU_20(UBUNTU.osName),

    UBUNTU_SERVER("Ubuntu_Server", fallback = listOf(UBUNTU)),
    DEBIAN("Debian"),
    FEDORA("Fedora"),
    FEDORA_SERVER("Fedora_Server"),
    REDHAT("RedHat"),
    UNKNOWN("Unknown");

    companion object {

        fun getByValue(value: String) : OSType {

            values().forEach {
                if (value == it.osName) {
                    return it
                }
            }
            return UNKNOWN
        }
    }
}