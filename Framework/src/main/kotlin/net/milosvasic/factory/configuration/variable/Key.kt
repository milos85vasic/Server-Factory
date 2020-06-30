package net.milosvasic.factory.configuration.variable

enum class Key(val key: String) {

    Home("HOME"),
    Type("TYPE"),
    City("CITY"),
    Name("NAME"),
    DbHost("HOST"),
    DbPort("PORT"),
    DbUser("DB_USER"),
    Country("COUNTRY"),
    Hostname("HOSTNAME"),
    Province("PROVINCE"),
    DbPassword("DB_PASSWORD"),
    Department("DEPARTMENT"),
    ServerHome("SERVER_HOME"),
    DockerHome("DOCKER_HOME"),
    Passphrase("PASSPHRASE"),
    DbDirectory("DB_DIRECTORY"),
    Organisation("ORGANISATION"),
    Certificates("CERTIFICATES"),
    DbPortExposed("PORT_EXPOSED"),
    RebootAllowed("REBOOT_ALLOWED"),
    DockerComposePath("DOCKER_COMPOSE_PATH"),
    TableDomains("TABLE_DOMAINS"),
    TableUsers("TABLE_USERS"),
    TableAliases("TABLE_ALIASES"),
    ViewDomains("VIEW_DOMAINS"),
    ViewUsers("VIEW_USERS"),
    ViewAliases("VIEW_ALIASES")
}