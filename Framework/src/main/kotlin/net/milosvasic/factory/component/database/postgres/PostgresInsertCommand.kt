package net.milosvasic.factory.component.database.postgres

import net.milosvasic.factory.component.database.command.DatabaseCommand

class PostgresInsertCommand(
        database: Postgres,
        private val table: String,
        private val fields: String,
        private val values: String,
        private val onConflictDo: String = "NOTHING"

) : DatabaseCommand(database) {

    override fun getDatabaseCommand(): String {

        val insert = "'INSERT INTO $table ($fields) VALUES ($values) ON CONFLICT DO $onConflictDo;'"
        val connection = database.connection
        return StringBuilder("PGPASSWORD=${connection.password} ${PostgresCommand.PSQL.obtain()}")
                .append(" --host=${connection.host} --port=${connection.port} --user=${connection.user}")
                .append(" -c $insert")
                .toString()
    }
}