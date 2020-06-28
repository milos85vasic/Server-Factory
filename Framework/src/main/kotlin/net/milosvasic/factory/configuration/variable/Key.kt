package net.milosvasic.factory.configuration.variable

enum class Key(val key: String) {

    Home("HOME"),
    City("CITY"),
    DbHost("HOST"),
    DbPort("PORT"),
    DbUser("DB_USER"),
    DbPortExposed("PORT_EXPOSED"),
    Name("NAME"),
    Country("COUNTRY"),
    Hostname("HOSTNAME"),
    Province("PROVINCE"),
    DbPassword("DB_PASSWORD"),
    Department("DEPARTMENT"),
    ServerHome("SERVER_HOME"),
    DockerHome("DOCKER_HOME"),
    Passphrase("PASSPHRASE"),
    Organisation("ORGANISATION"),
    Certificates("CERTIFICATES"),
    RebootAllowed("REBOOT_ALLOWED"),
    DockerComposePath("DOCKER_COMPOSE_PATH"),
    Type("TYPE"),
    TableDomains("TABLE_DOMAINS"),
    TableUsers("TABLE_USERS"),
    TableAliases("TABLE_ALIASES"),
    ViewDomains("VIEW_DOMAINS"),
    ViewUsers("VIEW_USERS"),
    ViewAliases("VIEW_ALIASES")
}