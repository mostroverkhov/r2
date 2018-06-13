package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.CoreRequesterBuilder
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.R2ClientFluentBuilder
import com.github.mostroverkhov.r2.core.RequesterFactory
import com.github.mostroverkhov.r2.core.internal.requester.metaData
import io.reactivex.Single
import io.rsocket.kotlin.DefaultPayload
import io.rsocket.kotlin.RSocketFactory.ClientRSocketFactory
import io.rsocket.kotlin.transport.ClientTransport

typealias FluentBuilder = R2ClientFluentBuilder<
        ClientRSocketFactory,
        ClientAcceptorBuilder,
        ClientTransport,
        Single<RequesterFactory>>

typealias AcceptorConfigurer = (ClientAcceptorBuilder) -> ClientAcceptorBuilder

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
        val acceptorBuilder = ClientAcceptorBuilder()
        val transport = clientTransport!!
        val configure = configurer!!

        val configuredAcceptorBuilder = configure(acceptorBuilder)
        val clientAcceptor = configuredAcceptorBuilder.build()
        val requesterCodec = configuredAcceptorBuilder.codecs().primary()

        val rSocket = withSetup(rSocketFactory)
                .acceptor { clientAcceptor::accept }
                .transport(transport)
                .start()

        return rSocket.map(::RequesterBuilder)
                .map { it.codec(requesterCodec) }
                .map(CoreRequesterBuilder::build)
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
                ?.mimeType {
                    it.dataMimeType(setupData.dataType)
                            .metadataMimeType(setupData.metadataType)
                }
                ?.setupPayload(
                        DefaultPayload(
                                setupData.data,
                                setupData.metadata)
                )
                ?: throw IllegalArgumentException("ClientRSocketFactory not set")
    }
}