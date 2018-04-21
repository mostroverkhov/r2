package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor

@Suppress("UNCHECKED_CAST")
abstract class ClientAcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket,
        T : ClientAcceptorBuilder<RequesterRSocket, HandlerRSocket, T>>
    : AcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket, T>() {

    private var serviceReader: ((RequesterFactory) -> ServiceReader)? = null

    abstract fun build(): ClientAcceptor<RequesterRSocket, HandlerRSocket>

    fun services(serviceReader: (RequesterFactory) -> ServiceReader): T {
        this.serviceReader = serviceReader
        return this as T
    }

    fun forTarget(rSocket: RequesterRSocket,
                  builder: (RequesterRSocket) -> RequesterBuilder)
            : ResponderTargetResolver {
        val requesterFactory = createRequesterFactory(rSocket, builder)
        val services = serviceReader?.invoke(requesterFactory) ?: emptyReader
        return createTargetResolver(services)
    }

    companion object {
        private val emptyReader = Services()
    }
}