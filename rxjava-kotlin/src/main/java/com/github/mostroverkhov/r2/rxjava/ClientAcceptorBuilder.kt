package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.rxjava.internal.adapters.HandlerRSocket
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor
import com.github.mostroverkhov.r2.core.CoreClientAcceptorBuilder
import com.github.mostroverkhov.r2.rxjava.internal.RequesterBuilder
import io.rsocket.kotlin.RSocket

class ClientAcceptorBuilder
    : CoreClientAcceptorBuilder<RSocket, RSocket, ClientAcceptorBuilder>() {
    override fun build() = RxjavaClientAcceptor { rSocket ->
        HandlerRSocket(targetResolver(rSocket, ::RequesterBuilder))
    }

    class RxjavaClientAcceptor internal constructor(
            private val handler: (RSocket) -> RSocket)
        : ClientAcceptor<RSocket, RSocket> {
        override fun accept(requester: RSocket): RSocket = handler(requester)

    }
}