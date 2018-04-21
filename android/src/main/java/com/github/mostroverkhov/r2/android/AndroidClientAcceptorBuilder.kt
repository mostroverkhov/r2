package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.AndroidRSocketHandler
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor
import com.github.mostroverkhov.r2.core.ClientAcceptorBuilder
import io.rsocket.android.RSocket

class AndroidClientAcceptorBuilder
    : ClientAcceptorBuilder<RSocket, RSocket, AndroidClientAcceptorBuilder>() {
    override fun build() = AndroidClientAcceptor { rSocket ->
        AndroidRSocketHandler(forTarget(rSocket, ::AndroidRequesterBuilder))
    }

    class AndroidClientAcceptor internal constructor(
            private val handler: (RSocket) -> RSocket)
        : ClientAcceptor<RSocket, RSocket> {
        override fun accept(requester: RSocket): RSocket = handler(requester)

    }
}