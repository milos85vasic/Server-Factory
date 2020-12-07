package net.milosvasic.factory.platform

import net.milosvasic.factory.common.DataHandler
import net.milosvasic.factory.operation.OperationResult
import net.milosvasic.factory.remote.Remote

class HostIpAddressDataHandler (private val remote: Remote) : DataHandler<OperationResult> {

    @Throws(IllegalArgumentException::class)
    override fun onData(data: OperationResult?) {

        data?.let {
            if (it.data.isNotEmpty() && it.data.isNotBlank()) {

                remote.setHostIp(it.data)
            }
        }
    }
}