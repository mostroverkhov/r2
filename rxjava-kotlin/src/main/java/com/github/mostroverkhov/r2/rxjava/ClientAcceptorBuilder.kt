package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.rxjava.adapters.RSocketHandler
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor
import com.github.mostroverkhov.r2.core.CoreClientAcceptorBuilder
import io.rsocket.kotlin.RSocket

class ClientAcceptorBuilder
    : CoreClientAcceptorBuilder<RSocket, RSocket, ClientAcceptorBuilder>() {
    override fun build() = RxjavaClientAcceptor { rSocket ->
        RSocketHandler(forTarget(rSocket, ::RequesterBuilder))
    }

    class RxjavaClientAcceptor internal constructor(
            private val handler: (RSocket) -> RSocket)
        : ClientAcceptor<RSocket, RSocket> {
        override fun accept(requester: RSocket): RSocket = handler(requester)

    }
}