package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.R2ServerFluentBuilder
import com.github.mostroverkhov.r2.core.internal.MimeType
import io.rsocket.kotlin.Closeable
import io.rsocket.kotlin.RSocketFactory
import io.rsocket.kotlin.transport.ServerTransport

class R2Server<T : Closeable> : R2ServerFluentBuilder<
        RSocketFactory.ServerRSocketFactory,
        ServerAcceptorBuilder,
        ServerTransport<T>,
        RSocketFactory.Start<T>>() {
    override fun transport(transport: ServerTransport<T>): RSocketFactory.Start<T> {
        assertState()
        val configurer = acceptorConfigurer!!
        val rSocketFactory = serverRSocketFactory!!

        val acceptorBuilder = ServerAcceptorBuilder()
        val acceptor = configurer(acceptorBuilder).build()
        return rSocketFactory.interceptors { it.connection(setupInterceptor()) }
                .acceptor { { setup, sendingSocket -> acceptor.accept(setup, sendingSocket) } }
                .transport(transport)
    }

    private fun setupInterceptor() = SetupInterceptor(MimeType)

}
