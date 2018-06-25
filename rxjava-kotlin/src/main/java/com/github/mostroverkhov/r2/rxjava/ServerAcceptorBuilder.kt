package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.CoreServerAcceptorBuilder
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor
import com.github.mostroverkhov.r2.rxjava.internal.RequesterBuilder
import com.github.mostroverkhov.r2.rxjava.internal.adapters.HandlerRSocket
import io.reactivex.Single
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.Setup
import java.nio.ByteBuffer

typealias HandlerFactory = (ByteBuffer, RSocket) -> RSocket
typealias Acceptor = ServerAcceptor<Setup, RSocket, Single<RSocket>>

class ServerAcceptorBuilder : CoreServerAcceptorBuilder<
        Setup,
        RSocket,
        Single<RSocket>,
        ServerAcceptorBuilder>() {

    override fun build(): Acceptor =
            RxjavaServerAcceptor { metadata, rSocket ->
                HandlerRSocket(
                        targetResolver(rSocket, metadata, ::RequesterBuilder))
            }

    private class RxjavaServerAcceptor(private val handlerFactory: HandlerFactory) : Acceptor {
        override fun accept(setup: Setup, rSocket: RSocket): Single<RSocket> =
                Single.just(handlerFactory(setup.metadata, rSocket))
    }
}
