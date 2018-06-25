package com.github.mostroverkhov.r2.core

abstract class R2ServerFluentBuilder<
        RSocketFactory,
        AcceptorBuilder,
        Transport,
        Closeable> {

    protected var rSocketFactory: RSocketFactory? = null
    protected var acceptorConfigurer: ((AcceptorBuilder) -> AcceptorBuilder)? = null

    open fun connectWith(rSocketFactory: RSocketFactory): R2ServerFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            Closeable> {
        this.rSocketFactory = rSocketFactory
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

    abstract fun transport(transport: Transport): Closeable
}