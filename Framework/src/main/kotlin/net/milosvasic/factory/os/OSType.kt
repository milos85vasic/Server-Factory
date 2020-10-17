package net.milosvasic.factory.os

enum class OSType(val osName: String) {

    CENTOS("CentOS"),
    CENTOS_8(CENTOS.osName),
    CENTOS_7("CentOS_7"),
    UBUNTU("Ubuntu"),
    DEBIAN("Debian"),
    FEDORA("Fedora"),
    REDHAT("RedHat"),
    UNKNOWN("Unknown")
}