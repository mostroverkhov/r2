package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor
import java.nio.ByteBuffer

@Suppress("UNCHECKED_CAST")
abstract class CoreServerAcceptorBuilder<
        SetupPayload,
        RequesterRSocket,
        HandlerRSocket,
        T : CoreServerAcceptorBuilder<
                SetupPayload,
                RequesterRSocket,
                HandlerRSocket,
                T>>

    : CoreAcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket, T>() {

    private var serviceReader:
            ((ConnectionContext, RequesterFactory) -> ServiceReader)? = null

    abstract fun build(): ServerAcceptor<SetupPayload, RequesterRSocket, HandlerRSocket>

    fun services(serviceReader:
                 (ConnectionContext, RequesterFactory) -> ServiceReader): T {
        this.serviceReader = serviceReader
        return this as T
    }

    fun targetResolver(rSocket: RequesterRSocket,
                       payloadMetadata: ByteBuffer,
                       builder: (RequesterRSocket) -> CoreRequesterBuilder)
            : ResponderTargetResolver {
        val requesterFactory = createRequesterFactory(rSocket, builder)
        val connectionContext = context(payloadMetadata)
        val svcReader = serviceReader ?: throw noServicesError()
        val services = svcReader(connectionContext, requesterFactory)
        return createTargetResolver(services)
    }

    companion object {
        private fun context(metadata: ByteBuffer): ConnectionContext =
                ConnectionContext(metadataCodec.decodeForConnection(metadata))

        private fun noServicesError() = IllegalStateException("Services must be set")
    }
}