package com.github.mostroverkhov.r2.core


abstract class R2ClientFluentBuilder<
        RSocketFactory,
        AcceptorBuilder,
        Transport,
        RequesterFactory> {

    protected var rSocketFactory: RSocketFactory? = null

    fun connectWith(rSocketFactory: RSocketFactory): R2ClientFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            RequesterFactory> {
        this.rSocketFactory = rSocketFactory
        return this
    }

    abstract fun metadata(metadata: Metadata): R2ClientFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            RequesterFactory>

    abstract fun transport(transport: Transport): R2ClientFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            RequesterFactory>

    abstract fun configureAcceptor(f: (AcceptorBuilder) -> AcceptorBuilder): R2ClientFluentBuilder<
            RSocketFactory,
            AcceptorBuilder,
            Transport,
            RequesterFactory>

    abstract fun start(): RequesterFactory
}
