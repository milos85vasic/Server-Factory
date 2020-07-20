package net.milosvasic.factory.component.installer.step.reboot

import net.milosvasic.factory.component.installer.step.RemoteOperationInstallationStep
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.execution.flow.implementation.CommandFlow
import net.milosvasic.factory.log
import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.operation.OperationResultListener
import net.milosvasic.factory.remote.Remote
import net.milosvasic.factory.remote.ssh.SSH
import net.milosvasic.factory.terminal.Terminal
import net.milosvasic.factory.terminal.command.EchoCommand
import net.milosvasic.factory.terminal.command.PingCommand
import net.milosvasic.factory.terminal.command.RebootCommand

class Reboot(private val timeoutInSeconds: Int = 120) : RemoteOperationInstallationStep<SSH>() {

    private var pingCount = 0
    private var helloCount = 0
    private val hello = "Hello"
    private val rebootScheduleTime = 3
    private var remote: Remote? = null
    private var terminal: Terminal? = null

    private val pingCallback = object : OperationResultListener {
        override fun onOperationPerformed(result: OperationResult) {

            when (result.operation) {

                is PingCommand -> {

                    if (result.success) {
                        hello()
                    } else {

                        if (pingCount <= timeoutInSeconds) {
                            ping()
                        } else {
                            log.e("Reboot timeout exceeded")
                            finish(false)
                        }
                    }
                }
            }
        }
    }

    private val helloCallback = object : OperationResultListener {
        override fun onOperationPerformed(result: OperationResult) {

            when (result.operation) {
                is EchoCommand -> {

                    if (result.success) {
                        finish(true)
                    } else {

                        if (helloCount <= timeoutInSeconds) {
                            hello()
                        } else {
                            log.e("Reboot timeout exceeded")
                            finish(false)
                        }
                    }
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getFlow(): CommandFlow {
        var rebootAllowed = false
        try {
            val path = PathBuilder()
                    .addContext(Context.Server)
                    .setKey(Key.RebootAllowed)
                    .build()

            rebootAllowed = Variable.get(path).toBoolean()
        } catch (e: IllegalStateException) {

            log.e(e)
            finish(false)
        }

        connection?.let { conn ->
            terminal = conn.getTerminal()
            remote = conn.getRemote()

            terminal?.let { term ->
                remote?.let { _ ->

                    return if (!rebootAllowed) {

                        CommandFlow()
                                .width(term)
                                .perform(EchoCommand("Reboot is not allowed by configuration"))
                    } else {

                        log.v("Reboot timeout in seconds: $timeoutInSeconds")
                        pingCount = 0
                        term.subscribe(pingCallback)

                        CommandFlow()
                                .width(conn)
                                .perform(RebootCommand(rebootScheduleTime))
                    }
                }
            }
        }
        throw IllegalArgumentException("No proper connection provided")
    }

    override fun getOperation() = RebootOperation()

    override fun finish(success: Boolean) {
        if (success && pingCount == 0) {

            try {

                log.v("Waiting for remote host to restart")
                for (x in 1..10) {
                    log.v("Counting:  $x")
                    Thread.sleep(1000)
                }
                ping()
            } catch (e: InterruptedException) {

                log.e(e)
                finish(false)
            }
        } else {

            terminal?.unsubscribe(pingCallback)
            connection?.unsubscribe(helloCallback)
            super.finish(success)
        }
    }

    private fun ping() {

        pingCount++
        log.v("Ping no. $pingCount")
        val host = remote?.host
        if (host == null) {

            log.e("No host to ping provided")
            finish(false)
        } else {

            terminal?.let { term ->
                try {
                    term.execute(PingCommand(host, 1))
                } catch (e: IllegalStateException) {

                    log.e(e)
                    finish(false)
                } catch (e: IllegalArgumentException) {

                    log.e(e)
                    finish(false)
                }
            }
        }
    }

    private fun hello() {

        helloCount++
        log.v("Hello no. $helloCount")
        val host = remote?.host
        if (host == null) {

            log.e("No host to hello provided")
            finish(false)
        } else {

            connection?.let { conn ->
                try {
                    if (helloCount == 1) {
                        conn.subscribe(helloCallback)
                    }
                    conn.execute(EchoCommand("Hello"))
                } catch (e: IllegalStateException) {

                    log.e(e)
                    finish(false)
                } catch (e: IllegalArgumentException) {

                    log.e(e)
                    finish(false)
                }
            }
        }
    }
}