package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.MimeType;
import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptor;
import com.github.mostroverkhov.r2.core.internal.responder.ServerFluentBuilder;
import io.rsocket.Closeable;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory.ServerRSocketFactory;
import io.rsocket.RSocketFactory.Start;
import io.rsocket.transport.ServerTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class R2Server<T extends Closeable> extends ServerFluentBuilder<
        ServerRSocketFactory,
        JavaAcceptorBuilder,
        ServerTransport<T>,
        Start<T>> {

    @Override
    public Start<T> transport(ServerTransport<T> transport) {
        assertState();
        JavaAcceptorBuilder acceptorBuilder = new JavaAcceptorBuilder();
        RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> acceptor =
                getAcceptorConfigurer()
                        .invoke(acceptorBuilder)
                        .build();

        return getServerRSocketFactory()
                .addConnectionPlugin(setupInterceptor())
                .acceptor(() -> (setup, sendRSocket) -> acceptor.accept(setup))
                .transport(transport);
    }

    @NotNull
    private SetupInterceptor setupInterceptor() {
        return new SetupInterceptor(
                MimeType.getDataType(),
                MimeType.getMetadataType());
    }
}
