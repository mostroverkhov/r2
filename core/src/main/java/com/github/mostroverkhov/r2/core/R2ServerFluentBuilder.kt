package com.github.mostroverkhov.r2.core

abstract class R2ServerFluentBuilder<
        RSocketFactory,
        AcceptorBuilder,
        Transport,
        Closeable> {

    protected var serverRSocketFactory: RSocketFactory? = null
    protected var acceptorConfigurer: ((AcceptorBuilder) -> AcceptorBuilder)? = null

    open fun connectWith(rSocketFactory: RSocketFactory): R2ServerFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            Closeable> {
        serverRSocketFactory = rSocketFactory
        return this
    }

    open fun configureAcceptor(f: (AcceptorBuilder) -> AcceptorBuilder): R2ServerFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            Closeable> {
        acceptorConfigurer = f
        return this
    }

    protected fun assertState() {
        if (serverRSocketFactory == null) {
            throw IllegalArgumentException("SocketFactory was not set")
        }
        if (acceptorConfigurer == null) {
            throw IllegalArgumentException("Acceptor was not configured")
        }
    }

    abstract fun transport(transport: Transport): Closeable
}