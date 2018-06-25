package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.R2ServerFluentBuilder
import com.github.mostroverkhov.r2.core.internal.MimeType
import com.github.mostroverkhov.r2.rxjava.internal.SetupInterceptor
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
        val rSocketFactory = rSocketFactory!!

        val acceptorBuilder = ServerAcceptorBuilder()
        val acceptor = configurer(acceptorBuilder).build()
        return rSocketFactory.interceptors { it.connection(setupInterceptor()) }
                .acceptor { { setup, sendingSocket -> acceptor.accept(setup, sendingSocket) } }
                .transport(transport)
    }

    private fun assertState() {
        if (rSocketFactory == null) {
            throw IllegalArgumentException("SocketFactory was not set")
        }
        if (acceptorConfigurer == null) {
            throw IllegalArgumentException("Acceptor was not configured")
        }
    }

    private fun setupInterceptor() = SetupInterceptor(MimeType)
}
