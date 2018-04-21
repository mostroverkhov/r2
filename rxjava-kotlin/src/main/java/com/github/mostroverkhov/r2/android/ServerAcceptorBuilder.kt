package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.RSocketHandler
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor
import com.github.mostroverkhov.r2.core.CoreServerAcceptorBuilder
import io.reactivex.Single
import io.rsocket.android.ConnectionSetupPayload
import io.rsocket.android.RSocket
import java.nio.ByteBuffer

typealias HandlerFactory = (ByteBuffer, RSocket) -> RSocket
typealias SetupPayload = ConnectionSetupPayload
typealias Acceptor = ServerAcceptor<SetupPayload, RSocket, Single<RSocket>>

class ServerAcceptorBuilder : CoreServerAcceptorBuilder<
        SetupPayload,
        RSocket,
        Single<RSocket>,
        ServerAcceptorBuilder>() {

    override fun build(): Acceptor =
            AndroidServerAcceptor { metadata, rSocket ->
                RSocketHandler(
                        forTarget(rSocket, metadata, ::RequesterBuilder))
            }

    private class AndroidServerAcceptor(private val handlerFactory: HandlerFactory) : Acceptor {
        override fun accept(setup: SetupPayload, rSocket: RSocket): Single<RSocket> =
                Single.just(handlerFactory(setup.metadata, rSocket))
    }
}
