package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.MimeType;
import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptor;
import com.github.mostroverkhov.r2.core.internal.responder.ServerFluentBuilder;
import com.github.mostroverkhov.rsocket.Closeable;
import com.github.mostroverkhov.rsocket.ConnectionSetupPayload;
import com.github.mostroverkhov.rsocket.RSocket;
import com.github.mostroverkhov.rsocket.RSocketFactory;
import com.github.mostroverkhov.rsocket.transport.ServerTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class R2Server<T extends Closeable> extends ServerFluentBuilder<
        RSocketFactory.ServerRSocketFactory,
        JavaAcceptorBuilder,
        ServerTransport<T>,
        RSocketFactory.Start<T>> {

    @Override
    public RSocketFactory.Start<T> transport(ServerTransport<T> transport) {
        assertState();
        JavaAcceptorBuilder acceptorBuilder = new JavaAcceptorBuilder();
        RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> acceptor =
                getAcceptorConfigurer()
                        .invoke(acceptorBuilder)
                        .build();

        return getServerRSocketFactory()
                .addConnectionInterceptor(setupInterceptor())
                .acceptor(() -> (setup, sendRSocket) -> acceptor.accept(setup))
                .transport(transport);
    }

    @NotNull
    private SetupInterceptor setupInterceptor() {
        return new SetupInterceptor(MimeType.INSTANCE);
    }
}
