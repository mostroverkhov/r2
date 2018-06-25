package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor

@Suppress("UNCHECKED_CAST")
abstract class CoreClientAcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket,
        T : CoreClientAcceptorBuilder<RequesterRSocket, HandlerRSocket, T>>
    : CoreAcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket, T>() {

    private var serviceReader: ((RequesterFactory) -> ServiceReader)? = null

    abstract fun build(): ClientAcceptor<RequesterRSocket, HandlerRSocket>

    fun services(serviceReader: (RequesterFactory) -> ServiceReader): T {
        this.serviceReader = serviceReader
        return this as T
    }

    fun targetResolver(rSocket: RequesterRSocket,
                       builder: (RequesterRSocket) -> CoreRequesterBuilder)
            : ResponderTargetResolver {
        val requesterFactory = createRequesterFactory(rSocket, builder)
        val services = serviceReader?.invoke(requesterFactory) ?: emptyReader
        return createTargetResolver(services)
    }

    companion object {
        private val emptyReader = Services()
    }
}