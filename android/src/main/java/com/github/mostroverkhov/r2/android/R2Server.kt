package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.internal.MimeType
import com.github.mostroverkhov.r2.core.internal.responder.ServerFluentBuilder
import io.rsocket.android.Closeable
import io.rsocket.android.ConnectionSetupPayload
import io.rsocket.android.RSocket
import io.rsocket.android.RSocketFactory.ServerRSocketFactory
import io.rsocket.android.RSocketFactory.Start
import io.rsocket.android.SocketAcceptor
import io.rsocket.android.transport.ServerTransport

class R2Server<T : Closeable> : ServerFluentBuilder<
        ServerRSocketFactory,
        AndroidAcceptorBuilder,
        ServerTransport<T>,
        Start<T>>() {
    override fun transport(transport: ServerTransport<T>): Start<T> {
        assertState()
        val configurer = acceptorConfigurer!!
        val rsocketFactory = serverRSocketFactory!!

        val acceptorBuilder = AndroidAcceptorBuilder()
        val acceptor = configurer(acceptorBuilder).build()
        return rsocketFactory
                .addConnectionPlugin(setupInterceptor())
                .acceptor { adapt(acceptor) }
                .transport(transport)
    }

    private fun setupInterceptor() = SetupInterceptor(
            MimeType.dataType,
            MimeType.metadataType)

    private fun adapt(acceptor: Acceptor): SocketAcceptor = object : SocketAcceptor {
        override fun accept(setup: ConnectionSetupPayload,
                            sendingSocket: RSocket) = acceptor.accept(setup)
    }
}
