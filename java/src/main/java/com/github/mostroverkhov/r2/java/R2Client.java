package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.*;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.r2.core.requester.RequesterBuilder;
import com.github.mostroverkhov.r2.core.internal.requester.ClientSetup;
import com.github.mostroverkhov.r2.core.internal.requester.SetupMetadata;
import com.github.mostroverkhov.r2.core.internal.requester.ClientFluentBuilder;
import com.github.mostroverkhov.r2.core.internal.requester.RequesterConfigurer;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.PayloadImpl;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static io.rsocket.RSocketFactory.*;

public class R2Client extends ClientFluentBuilder<ClientRSocketFactory,
        ClientTransport,
        RSocket,
        Mono<RequesterFactory>> {

    @NotNull
    @Override
    public ClientFluentBuilder<RSocketFactory.ClientRSocketFactory,
            ClientTransport,
            RSocket,
            Mono<RequesterFactory>> metadata(@NotNull Metadata metadata) {

        setClientRSocketFactory(new ClientSetup()
                .metadata(metadata)
                .setupMetadata(setup -> connectionSetup(getClientRSocketFactory(), setup)));

        return this;
    }

    @NotNull
    @Override
    public RequesterConfigurer<Mono<RequesterFactory>> transport(ClientTransport clientTransport) {
        ClientRSocketFactory factory = getClientRSocketFactory();
        assertFactory(factory);
        return new JavaRequesterConfigurer(factory.transport(clientTransport).start());
    }

    private static ClientRSocketFactory connectionSetup(ClientRSocketFactory factory,
                                                        SetupMetadata setup) {
        assertFactory(factory);
        return factory
                .dataMimeType(setup.getDataType())
                .metadataMimeType(setup.getMetadataType())
                .setupPayload(
                        new PayloadImpl(
                                setup.getData(),
                                setup.getMetadata())
                );
    }

    private static void assertFactory(ClientRSocketFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("ClientRSocketFactory was not set");
        }
    }

    private static class JavaRequesterConfigurer
            implements RequesterConfigurer<Mono<RequesterFactory>> {

        private final Mono<RSocket> rSocket;

        public JavaRequesterConfigurer(Mono<RSocket> rSocket) {
            this.rSocket = rSocket;
        }

        @NotNull
        @Override
        public Mono<RequesterFactory> configureRequester(@NotNull Function1<
                ? super RequesterBuilder,
                ? extends RequesterBuilder> f) {
            return rSocket.map(JavaRequesterBuilder::new)
                    .map(f::invoke)
                    .map(RequesterBuilder::build);
        }
    }
}
