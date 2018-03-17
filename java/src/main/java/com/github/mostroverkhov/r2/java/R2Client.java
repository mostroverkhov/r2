package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.internal.requester.ClientFluentBuilder;
import com.github.mostroverkhov.r2.core.internal.requester.ClientSetup;
import com.github.mostroverkhov.r2.core.internal.requester.SetupData;
import com.github.mostroverkhov.r2.core.requester.RequesterBuilder;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.rsocket.RSocket;
import com.github.mostroverkhov.rsocket.RSocketFactory;
import com.github.mostroverkhov.rsocket.RSocketFactory.ClientRSocketFactory;
import com.github.mostroverkhov.rsocket.transport.ClientTransport;
import com.github.mostroverkhov.rsocket.util.PayloadImpl;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;


public class R2Client extends ClientFluentBuilder<
        ClientRSocketFactory,
        ClientTransport,
        RSocket,
        Mono<RequesterFactory>> {

    private Function1<
            ? super RequesterBuilder,
            ? extends RequesterBuilder> requesterConfigurer;
    private Metadata metadata;
    private ClientTransport clientTransport;

    @NotNull
    @Override
    public ClientFluentBuilder<
            ClientRSocketFactory,
            ClientTransport,
            RSocket,
            Mono<RequesterFactory>> metadata(@NotNull Metadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @NotNull
    @Override
    public ClientFluentBuilder<
            ClientRSocketFactory,
            ClientTransport,
            RSocket,
            Mono<RequesterFactory>> transport(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        return this;
    }

    @NotNull
    @Override
    public ClientFluentBuilder<
            ClientRSocketFactory,
            ClientTransport,
            RSocket,
            Mono<RequesterFactory>> configureRequester(
            @NotNull Function1<
                    ? super RequesterBuilder,
                    ? extends RequesterBuilder> f) {
        requesterConfigurer = f;
        return this;
    }

    @Override
    public Mono<RequesterFactory> start() {
        assertState();
        SetupData setupData = ClientSetup.clientSetupMetaData(metadata);
        Mono<RSocket> rSocket = connectionSetup(getClientRSocketFactory(), setupData)
                .transport(clientTransport)
                .start();
        Mono<RequesterFactory> requesterFactory = rSocket
                .map(JavaRequesterBuilder::new)
                .map(requesterConfigurer::invoke)
                .map(RequesterBuilder::build);

        return requesterFactory;
    }

    private static ClientRSocketFactory connectionSetup(ClientRSocketFactory factory,
                                                        SetupData setup) {
        return factory
                .dataMimeType(setup.getDataType())
                .metadataMimeType(setup.getMetadataType())
                .setupPayload(
                        new PayloadImpl(
                                setup.getData(),
                                setup.getMetadata())
                );
    }

    private void assertState() {
        assertArg(getClientRSocketFactory(), "ClientRSocketFactory");
        assertArg(clientTransport, "ClientTransport");
        assertArg(requesterConfigurer, "RequesterConfigurer");
    }

    private static void assertArg(Object arg, String name) {
        if (arg == null) {
            throw new IllegalArgumentException(name + " was not set");
        }
    }
}
