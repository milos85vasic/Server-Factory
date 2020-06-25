package net.milosvasic.factory.configuration

enum class VariableKey(val key: String) {

    Home("HOME"),
    Hostname("HOSTNAME"),
    ServerHome("SERVER_HOME"),
    DockerHome("DOCKER_HOME"),
    Certificates("CERTIFICATES"),
    RebootAllowed("REBOOT_ALLOWED"),
    DockerComposePath("DOCKER_COMPOSE_PATH"),
    DbHost("DBHOST"),
    DbPort("DB_PORT"),
    DbUser("DB_USER"),
    DbPassword("DB_PASSWORD")
}