package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.requester.ClientFluentBuilder
import com.github.mostroverkhov.r2.core.internal.requester.SetupData
import com.github.mostroverkhov.r2.core.internal.requester.clientSetupMetaData
import com.github.mostroverkhov.r2.core.requester.RequesterBuilder
import com.github.mostroverkhov.r2.core.requester.RequesterFactory
import io.reactivex.Single
import io.rsocket.android.RSocket
import io.rsocket.android.RSocketFactory.ClientRSocketFactory
import io.rsocket.android.transport.ClientTransport
import io.rsocket.android.util.PayloadImpl

typealias FluentBuilder = ClientFluentBuilder<
        ClientRSocketFactory,
        ClientTransport,
        RSocket,
        Single<RequesterFactory>>

class R2Client : FluentBuilder() {
    private var requesterConfigurer: ((RequesterBuilder) -> RequesterBuilder)? = null
    private var metadata: Metadata? = null
    private var clientTransport: ClientTransport? = null

    override fun metadata(metadata: Metadata): FluentBuilder {
        this.metadata = metadata
        return this
    }

    override fun transport(transport: ClientTransport): FluentBuilder {
        clientTransport = transport
        return this
    }

    override fun configureRequester(f: (RequesterBuilder) -> RequesterBuilder): FluentBuilder {
        requesterConfigurer = f
        return this
    }

    override fun start(): Single<RequesterFactory> {
        assertState()

        val transport = clientTransport!!
        val configure = requesterConfigurer!!
        val setupData = clientSetupMetaData(metadata)

        val rSocket = connectionSetup(clientRSocketFactory, setupData)
                .transport { transport }
                .start()

        return rSocket.map { AndroidRequesterBuilder(it) }
                .map { builder -> configure(builder).build() }
    }

    private fun assertState() {
        assertArg(clientRSocketFactory, "ClientRSocketFactory")
        assertArg(clientTransport, "ClientTransport")
        assertArg(requesterConfigurer, "RequesterConfigurer")
    }

    private fun assertArg(arg: Any?, name: String) {
        if (arg == null) {
            throw IllegalArgumentException("$name was not set")
        }
    }

    private fun connectionSetup(factory: ClientRSocketFactory?,
                                setup: SetupData): ClientRSocketFactory {
        return factory
                ?.dataMimeType(setup.dataType)
                ?.metadataMimeType(setup.metadataType)
                ?.setupPayload(
                        PayloadImpl(
                                setup.data,
                                setup.metadata)
                ) ?: throw IllegalArgumentException("ClientRSocketFactory not set")
    }
}