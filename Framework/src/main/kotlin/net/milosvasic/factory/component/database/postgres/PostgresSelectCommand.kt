package net.milosvasic.factory.component.database.postgres

import net.milosvasic.factory.component.database.command.DatabaseCommand

class PostgresSelectCommand(

        database: Postgres,
        private val table: String,
        private val selectFields: String,
        private val queryField: String,
        private val value: String
) : DatabaseCommand(database) {

    override fun getDatabaseCommand(): String {

        val select = getSelectStatement()
        val connection = database.connection
        return StringBuilder("PGPASSWORD=${connection.password} ${PostgresCommand.PSQL.obtain()}")
                .append(" --host=${connection.host} --port=${connection.port} --user=${connection.user}")
                .append(" -d ${database.name} -c \"$select\"")
                .toString()
    }

    fun getSelectStatement() = "SELECT ($selectFields) FROM $table WHERE $queryField = '$value'"
}