package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptor
import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptorBuilder
import io.reactivex.Single
import io.rsocket.android.ConnectionSetupPayload
import io.rsocket.android.RSocket
import java.nio.ByteBuffer

typealias HandlerFactory = (ByteBuffer) -> RSocket
typealias SetupPayload = ConnectionSetupPayload
typealias Acceptor = RequestAcceptor<SetupPayload, Single<RSocket>>

class AndroidAcceptorBuilder : RequestAcceptorBuilder<
        SetupPayload,
        Single<RSocket>,
        AndroidAcceptorBuilder>() {

    override fun build(): Acceptor
            = AndroidRequestAcceptor { AndroidRSocketHandler(targetResolver(it)) }

    private class AndroidRequestAcceptor(private val handlerFactory: HandlerFactory) : Acceptor {
        override fun accept(setup: SetupPayload): Single<RSocket> =
                Single.just(handlerFactory(setup.metadata))
    }
}
