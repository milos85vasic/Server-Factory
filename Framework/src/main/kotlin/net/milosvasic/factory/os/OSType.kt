package net.milosvasic.factory.os

enum class OSType(val osName: String) {

    CENTOS("CentOS"),
    CENTOS_8(CENTOS.osName),
    CENTOS_7("CentOS_7"),
    UBUNTU("Ubuntu"),
    UBUNTU_20(UBUNTU.osName),
    UBUNTU_19("Ubuntu_19"),
    UBUNTU_18("Ubuntu_18"),
    UBUNTU_SERVER("Ubuntu_Server"),
    DEBIAN("Debian"),
    FEDORA("Fedora"),
    FEDORA_SERVER("Fedora_Server"),
    REDHAT("RedHat"),
    UNKNOWN("Unknown")
}