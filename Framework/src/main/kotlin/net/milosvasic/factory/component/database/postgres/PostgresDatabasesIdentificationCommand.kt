package net.milosvasic.factory.component.database.postgres

import net.milosvasic.factory.terminal.TerminalCommand

class PostgresDatabasesIdentificationCommand(

        host: String,
        port: Int,
        user: String,
        password: String
) : TerminalCommand(

        StringBuilder("PGPASSWORD=$password ${PostgresCommand.PSQL.obtain()}")
                .append(" --host=$host --port=$port --user=$user")
                .append(" --list")
                .toString()
)