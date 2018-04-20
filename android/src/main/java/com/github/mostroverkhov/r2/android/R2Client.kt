package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.R2ClientFluentBuilder
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.requester.metaData
import com.github.mostroverkhov.r2.core.RequesterBuilder
import com.github.mostroverkhov.r2.core.RequesterFactory
import io.reactivex.Single
import io.rsocket.android.RSocketFactory.ClientRSocketFactory
import io.rsocket.android.transport.ClientTransport
import io.rsocket.android.util.PayloadImpl

typealias FluentBuilder = R2ClientFluentBuilder<
        ClientRSocketFactory,
        AndroidClientAcceptorBuilder,
        ClientTransport,
        Single<RequesterFactory>>

typealias AcceptorConfigurer = (AndroidClientAcceptorBuilder) -> AndroidClientAcceptorBuilder

class R2Client : FluentBuilder() {
    private var configurer: AcceptorConfigurer? = null
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

    override fun configureAcceptor(f: AcceptorConfigurer): FluentBuilder {
        configurer = f
        return this
    }

    override fun start(): Single<RequesterFactory> {
        assertState()
        val acceptorBuilder = AndroidClientAcceptorBuilder()
        val transport = clientTransport!!
        val configure = configurer!!

        val configuredAcceptorBuilder = configure(acceptorBuilder)
        val clientAcceptor = configuredAcceptorBuilder.build()
        val requesterCodec = configuredAcceptorBuilder.codecs().primary()

        val rSocket = withSetup(rSocketFactory)
                .acceptor { clientAcceptor::accept }
                .transport(transport)
                .start()

        return rSocket.map(::AndroidRequesterBuilder)
                .map { it.codec(requesterCodec) }
                .map(RequesterBuilder::build)
    }

    private fun assertState() {
        assertArg(clientTransport, "ClientRSocketFactory")
        assertArg(clientTransport, "ClientTransport")
        assertArg(configurer, "RequesterConfigurer")
    }

    private fun assertArg(arg: Any?, name: String) {
        if (arg == null) {
            throw IllegalArgumentException("$name was not set")
        }
    }

    private fun withSetup(factory: ClientRSocketFactory?): ClientRSocketFactory {
        val setupData = metaData(metadata)
        return factory
                ?.dataMimeType(setupData.dataType)
                ?.metadataMimeType(setupData.metadataType)
                ?.setupPayload(
                        PayloadImpl(
                                setupData.data,
                                setupData.metadata)
                )
                ?: throw IllegalArgumentException("ClientRSocketFactory not set")
    }
}