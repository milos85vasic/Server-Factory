package net.milosvasic.factory.configuration.variable

enum class Key(val key: String) {

    Home("HOME"),
    Hostname("HOSTNAME"),
    ServerHome("SERVER_HOME"),
    DockerHome("DOCKER_HOME"),
    Certificates("CERTIFICATES"),
    RebootAllowed("REBOOT_ALLOWED"),
    DockerComposePath("DOCKER_COMPOSE_PATH"),
    DbHost("DB_HOST"),
    DbPort("DB_PORT"),
    DbUser("DB_USER"),
    DbName("DB_NAME"),
    DbPassword("DB_PASSWORD")
}