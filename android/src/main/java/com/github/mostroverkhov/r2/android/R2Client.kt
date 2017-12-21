package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.requester.ClientFluentBuilder
import com.github.mostroverkhov.r2.core.internal.requester.RequesterConfigurer
import com.github.mostroverkhov.r2.core.internal.requester.SetupData
import com.github.mostroverkhov.r2.core.internal.requester.setupData
import com.github.mostroverkhov.r2.core.requester.RequesterBuilder
import com.github.mostroverkhov.r2.core.requester.RequesterFactory
import io.reactivex.Single
import io.rsocket.android.RSocket
import io.rsocket.android.RSocketFactory.ClientRSocketFactory
import io.rsocket.android.transport.ClientTransport
import io.rsocket.android.util.PayloadImpl

class R2Client : ClientFluentBuilder<
        ClientRSocketFactory,
        ClientTransport,
        RSocket,
        Single<RequesterFactory>>() {

    override fun metadata(metadata: Metadata): ClientFluentBuilder<
            ClientRSocketFactory,
            ClientTransport,
            RSocket,
            Single<RequesterFactory>> {
        val setupMetadata = setupData(metadata)
        clientRSocketFactory = connectionSetup(clientRSocketFactory, setupMetadata)
        return this
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

    override fun transport(transport: ClientTransport)
            : RequesterConfigurer<Single<RequesterFactory>> {
        val rSocket = clientRSocketFactory
                ?.transport { transport }
                ?.start()
                ?: throw IllegalArgumentException("ClientRSocketFactory not set")
        return AndroidRequesterConfigurer(rSocket)
    }

    internal class AndroidRequesterConfigurer(private val rSocket: Single<RSocket>)
        : RequesterConfigurer<Single<RequesterFactory>> {
        override fun configureRequester(f: (RequesterBuilder) -> RequesterBuilder): Single<RequesterFactory> {
            return rSocket
                    .map { AndroidRequesterBuilder(it) }
                    .map { f(it) }
                    .map { it.build() }
        }
    }
}