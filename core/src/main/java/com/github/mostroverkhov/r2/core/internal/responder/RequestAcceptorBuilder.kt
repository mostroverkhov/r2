package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.RouteDecoder
import com.github.mostroverkhov.r2.core.responder.CodecReader
import com.github.mostroverkhov.r2.core.responder.ConnectionContext
import com.github.mostroverkhov.r2.core.responder.ServiceReader
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.StringRouteCodec
import java.nio.ByteBuffer

abstract class RequestAcceptorBuilder<SetupPayload, HandlerRSocket> {
    private lateinit var serviceReader: (ConnectionContext) -> ServiceReader
    private lateinit var codecReader: CodecReader
    private var routeDecoderF = defaultRouteDecoder

    fun services(serviceReader: (ConnectionContext) -> ServiceReader)
            : RequestAcceptorBuilder<SetupPayload, HandlerRSocket> {
        this.serviceReader = serviceReader
        return this
    }

    fun codecs(codecReader: CodecReader): RequestAcceptorBuilder<SetupPayload, HandlerRSocket> {
        this.codecReader = codecReader
        return this
    }

    fun routeDecoder(routeDecoderF: (CodecReader) -> RouteDecoder)
            : RequestAcceptorBuilder<SetupPayload, HandlerRSocket> {
        this.routeDecoderF = routeDecoderF
        return this
    }

    protected fun targetResolver(payloadMetadata: ByteBuffer): ResponderTargetResolver {
        return ResponderTargetResolver(
                serviceReader(metadata(payloadMetadata)),
                metadataCodec,
                routeDecoderF(codecReader))
    }

    abstract fun build(): RequestAcceptor<SetupPayload, HandlerRSocket>

    private fun metadata(payloadMetadata: ByteBuffer) = ctxCreator(payloadMetadata)

    companion object {
        private var defaultRouteDecoder: (CodecReader) -> RouteDecoder =
                { StringRouteCodec().decoder(it) }

        private val metadataCodec = MetadataCodec()

        private val ctxCreator: (ByteBuffer) -> ConnectionContext =
                { setupMetadata -> ConnectionContext(metadataCodec.decodeForConnection(setupMetadata)) }
    }
}