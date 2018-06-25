package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.ServiceMethod
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.RemoteServiceMethod
import com.github.mostroverkhov.r2.core.internal.ServiceMethodEncoder
import java.nio.ByteBuffer

sealed class Call : ServiceMethod {
    abstract val interaction: Interaction
}

data class RequesterCall internal constructor(
        private val serviceMethodEncoder: ServiceMethodEncoder,
        internal val metadataCodec: MetadataCodec,
        override val dataCodec: DataCodec,
        override val service: String,
        override val method: String,
        override val interaction: Interaction,
        internal val responsePayloadType: Class<*>) : Call(), RemoteServiceMethod {

    private var params: CallParams? = null

    internal fun params(params: CallParams): RequesterCall {
        this.params = params
        return this
    }

    fun params(): CallParams = params!!

    fun encodeData(payloadT: Any?): ByteBuffer = payloadT
            ?.let { dataCodec.encode(it) }
            ?: emptyData

    fun decodeData(data: ByteBuffer): Any = dataCodec
            .decode(data, responsePayloadType)

    fun encodeMetadata(): ByteBuffer = metadataCodec.encode(
            requestMetadata(
                    params!!.metadata,
                    this))

    companion object {
        private val emptyData = ByteBuffer.allocate(0)
    }

    private fun requestMetadata(argMetadata: Metadata?,
                                serviceMethod: RemoteServiceMethod): Metadata {

        return Metadata.RequestBuilder()
                .metadata(argMetadata)
                .svcMethod(serviceMethodEncoder.encode(serviceMethod))
                .build()
    }
}

internal data class TerminationCall(override val service: String,
                                    override val method: String,
                                    override val interaction: Interaction
) : Call()

data class CallParams(val data: Any?, val metadata: Metadata?) {

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

        fun build(): CallParams {
            validate()
            return CallParams(data, metadata)
        }

        private fun validate() {
            if (data == null && interaction == Interaction.CHANNEL) {
                throw IllegalStateException(
                        "Channel requests are expected to have data")
            }
        }
    }
}
