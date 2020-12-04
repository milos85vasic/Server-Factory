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
import net.milosvasic.factory.terminal.command.SleepCommand

class Reboot(private val timeoutInSeconds: Int = 480) : RemoteOperationInstallationStep<SSH>() {

    private var pingCount = 0
    private val maxHellos = 10
    private var helloCount = 0
    private val hello = "Hello"
    private val numberOfPings = 3
    private var hasRestarted = false
    private val rebootScheduleTime = 3
    private var remote: Remote? = null
    private var terminal: Terminal? = null

    private val pingCallback = object : OperationResultListener {
        override fun onOperationPerformed(result: OperationResult) {

            when (result.operation) {

                is PingCommand -> {
                    if (result.success) {
                        if (hasRestarted) {
                            try {

                                hello()
                            } catch (e: IllegalArgumentException) {

                                log.e(e)
                                finish(false)
                            } catch (e: IllegalStateException) {

                                log.e(e)
                                finish(false)
                            }
                        } else {

                            log.v("Remote host is not restarted yet")
                            try {

                                ping()
                            } catch (e: IllegalStateException) {

                                log.e(e)
                                finish(false)
                            }
                        }
                    } else {

                        if (!hasRestarted) {
                            pingCount = 0
                            hasRestarted = true
                            log.i("Remote host has been restarted")
                        }
                        if (pingCount <= (timeoutInSeconds / numberOfPings)) {
                            try {

                                ping()
                            } catch (e: IllegalStateException) {

                                log.e(e)
                                finish(false)
                            }
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
                is SleepCommand -> {
                    try {

                        hello()
                    } catch (e: IllegalArgumentException) {

                        log.e(e)
                        finish(false)
                    } catch (e: IllegalStateException) {

                        log.e(e)
                        finish(false)
                    }
                }
                is EchoCommand -> {
                    if (result.success) {

                        finish(true)
                    } else {

                        if (helloCount <= maxHellos) {
                            try {

                                hello(true)
                            } catch (e: IllegalArgumentException) {

                                log.e(e)
                                finish(false)
                            } catch (e: IllegalStateException) {

                                log.e(e)
                                finish(false)
                            }
                        } else {

                            log.e("Hello retries exceeded")
                            finish(false)
                        }
                    }
                }
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun getFlow(): CommandFlow {

        val rebootAllowed = isRebootAllowed()
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
                if (isRebootAllowed()) {

                    ping()
                } else {

                    terminal?.unsubscribe(pingCallback)
                    connection?.unsubscribe(helloCallback)
                    super.finish(success)
                }
            } catch (e: InterruptedException) {

                log.e(e)
                terminal?.unsubscribe(pingCallback)
                connection?.unsubscribe(helloCallback)
                finish(false)
            } catch (e: IllegalStateException) {

                log.e(e)
                terminal?.unsubscribe(pingCallback)
                connection?.unsubscribe(helloCallback)
                finish(false)
            }
        } else {

            terminal?.unsubscribe(pingCallback)
            connection?.unsubscribe(helloCallback)
            super.finish(success)
        }
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    private fun ping() {

        pingCount++
        log.v("Ping no. $pingCount")
        val host = remote?.getHost()
        if (host == null) {

            log.e("No host to ping provided")
            finish(false)
        } else {

            terminal?.let { term ->
                try {

                    term.execute(PingCommand(host, numberOfPings))
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

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    private fun hello(sleep: Boolean = false) {

        helloCount++
        log.v("Hello no. $helloCount")
        val host = remote?.getHost()
        if (host == null) {

            log.e("No host to hello provided")
            finish(false)
        } else {

            connection?.let { conn ->
                try {
                    if (helloCount == 1) {
                        conn.subscribe(helloCallback)
                    }
                    if (sleep) {

                        conn.execute(SleepCommand(5))
                    } else {

                        conn.execute(EchoCommand(hello))
                    }
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

    private fun isRebootAllowed(): Boolean {

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
        return rebootAllowed
    }
}