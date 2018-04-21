package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.RSocketHandler
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor
import com.github.mostroverkhov.r2.core.CoreClientAcceptorBuilder
import io.rsocket.android.RSocket

class ClientAcceptorBuilder
    : CoreClientAcceptorBuilder<RSocket, RSocket, ClientAcceptorBuilder>() {
    override fun build() = AndroidClientAcceptor { rSocket ->
        RSocketHandler(forTarget(rSocket, ::RequesterBuilder))
    }

    class AndroidClientAcceptor internal constructor(
            private val handler: (RSocket) -> RSocket)
        : ClientAcceptor<RSocket, RSocket> {
        override fun accept(requester: RSocket): RSocket = handler(requester)

    }
}