package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.RouteDecoder
import com.github.mostroverkhov.r2.core.responder.CodecReader
import com.github.mostroverkhov.r2.core.responder.ConnectionContext
import com.github.mostroverkhov.r2.core.responder.ServiceReader
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.StringRouteCodec
import java.nio.ByteBuffer

@Suppress("UNCHECKED_CAST")
abstract class RequestAcceptorBuilder<SetupPayload,
        HandlerRSocket,
        T : RequestAcceptorBuilder<SetupPayload, HandlerRSocket, T>> {
    private lateinit var serviceReader: (ConnectionContext) -> ServiceReader
    private lateinit var codecReader: CodecReader
    private var routeDecoderF = defaultRouteDecoder

    open fun services(serviceReader: (ConnectionContext) -> ServiceReader): T {
        this.serviceReader = serviceReader
        return this as T
    }

    open fun codecs(codecReader: CodecReader): T {
        this.codecReader = codecReader
        return this as T
    }

    open fun routeDecoder(routeDecoderF: (CodecReader) -> RouteDecoder): T {
        this.routeDecoderF = routeDecoderF
        return this as T
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