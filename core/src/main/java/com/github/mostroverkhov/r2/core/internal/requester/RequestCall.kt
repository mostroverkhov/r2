package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.Route
import java.nio.ByteBuffer

sealed class Call {
    abstract val interaction: Interaction
}

data class RequestCall(internal val metadataFactory: (Route) -> Metadata,
                       internal val metadataCodec: MetadataCodec,
                       override val dataCodec: DataCodec,
                       override val service: String,
                       override val method: String,
                       val arg: Any,
                       override val interaction: Interaction,
                       internal val responsePayloadType: Class<*>) : Call(), Route {

    fun encodeData(payloadT: Any): ByteBuffer = dataCodec.encode(payloadT)

    fun decodeData(data: ByteBuffer): Any = dataCodec.decode(data, responsePayloadType)

    fun encodeMetadata(): ByteBuffer = metadataCodec.encode(metadataFactory(this))
}

data class CloseCall(override val interaction: Interaction) : Call()



