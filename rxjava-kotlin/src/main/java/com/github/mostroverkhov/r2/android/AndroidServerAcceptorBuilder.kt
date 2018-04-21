package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.AndroidRSocketHandler
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor
import com.github.mostroverkhov.r2.core.ServerAcceptorBuilder
import io.reactivex.Single
import io.rsocket.android.ConnectionSetupPayload
import io.rsocket.android.RSocket
import java.nio.ByteBuffer

typealias HandlerFactory = (ByteBuffer, RSocket) -> RSocket
typealias SetupPayload = ConnectionSetupPayload
typealias Acceptor = ServerAcceptor<SetupPayload, RSocket, Single<RSocket>>

class AndroidServerAcceptorBuilder : ServerAcceptorBuilder<
        SetupPayload,
        RSocket,
        Single<RSocket>,
        AndroidServerAcceptorBuilder>() {

    override fun build(): Acceptor =
            AndroidServerAcceptor { metadata, rSocket ->
                AndroidRSocketHandler(
                        forTarget(rSocket, metadata, ::AndroidRequesterBuilder))
            }

    private class AndroidServerAcceptor(private val handlerFactory: HandlerFactory) : Acceptor {
        override fun accept(setup: SetupPayload, rSocket: RSocket): Single<RSocket> =
                Single.just(handlerFactory(setup.metadata, rSocket))
    }
}
