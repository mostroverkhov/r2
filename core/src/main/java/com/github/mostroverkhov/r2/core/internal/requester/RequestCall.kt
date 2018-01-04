package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.Route
import com.github.mostroverkhov.r2.core.RouteEncoder
import java.nio.ByteBuffer

sealed class Call {
    abstract val interaction: Interaction
}

data class RequestCall internal constructor(internal val routeEncoder: RouteEncoder,
                                            internal val metadataCodec: MetadataCodec,
                                            override val dataCodec: DataCodec,
                                            override val service: String,
                                            override val method: String,
                                            override val interaction: Interaction,
                                            internal val responsePayloadType: Class<*>) : Call(), Route {
    private var args: ActionArgs? = null

    internal fun setArgs(args: ActionArgs): RequestCall {
        this.args = args
        return this
    }

    fun getArgs(): ActionArgs {
        return args!!
    }


    fun encodeData(payloadT: Any?): ByteBuffer = payloadT?.let { dataCodec.encode(it) } ?: emptyData

    fun decodeData(data: ByteBuffer): Any = dataCodec.decode(data, responsePayloadType)

    fun encodeMetadata(): ByteBuffer = metadataCodec.encode(requestMetadata(args!!.metadata, this))

    companion object {
        private val emptyData = ByteBuffer.allocate(0)
    }

    private fun requestMetadata(argMetadata: Metadata?,
                                route: Route): Metadata {

        return Metadata.RequestBuilder()
                .metadata(argMetadata)
                .route(routeEncoder.encode(route))
                .build()
    }
}

internal data class CloseCall(override val interaction: Interaction) : Call()

data class ActionArgs(val data: Any?, val metadata: Metadata?) {

    class Builder(private val interaction: Interaction) {
        private var data: Any? = null
        private var metadata: Metadata? = null

        fun data(data: Any): Builder {
            this.data = data
            return this
        }

        fun metadata(metadata: Metadata): Builder {
            this.metadata = metadata
            return this
        }

        fun build(): ActionArgs {
            validate()
            return ActionArgs(data, metadata)
        }

        private fun validate() {
            if (data == null && interaction == Interaction.CHANNEL) {
                throw IllegalStateException("Channel requests are expected to have data")
            }
        }
    }
}
