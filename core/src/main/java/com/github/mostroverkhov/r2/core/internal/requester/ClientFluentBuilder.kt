package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.requester.RequesterBuilder


abstract class ClientFluentBuilder<
        RSocketFactory,
        Transport,
        RSocket,
        RequesterFactory> {

    protected var clientRSocketFactory: RSocketFactory? = null

    fun connectWith(rSocketFactory: RSocketFactory): ClientFluentBuilder<
            RSocketFactory,
            Transport,
            RSocket,
            RequesterFactory> {
        clientRSocketFactory = rSocketFactory
        return this
    }

    abstract fun metadata(metadata: Metadata): ClientFluentBuilder<
            RSocketFactory,
            Transport,
            RSocket,
            RequesterFactory>

    abstract fun transport(transport: Transport): ClientFluentBuilder<
            RSocketFactory,
            Transport,
            RSocket,
            RequesterFactory>

    abstract fun configureRequester(f: (RequesterBuilder) -> RequesterBuilder): ClientFluentBuilder<
            RSocketFactory,
            Transport,
            RSocket,
            RequesterFactory>

    abstract fun start(): RequesterFactory
}
