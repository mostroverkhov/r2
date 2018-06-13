package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.MimeType;
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor;
import com.github.mostroverkhov.r2.core.R2ServerFluentBuilder;
import io.rsocket.Closeable;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory.ServerRSocketFactory;
import io.rsocket.RSocketFactory.Start;
import io.rsocket.transport.ServerTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public class R2Server<T extends Closeable> extends R2ServerFluentBuilder<
        ServerRSocketFactory,
    ServerAcceptorBuilder,
        ServerTransport<T>,
        Start<T>> {

    @Override
    public Start<T> transport(ServerTransport<T> transport) {
        assertState();
        ServerAcceptorBuilder acceptorBuilder = new ServerAcceptorBuilder();
        ServerAcceptor<ConnectionSetupPayload, RSocket,Mono<RSocket>> acceptor =
                getAcceptorConfigurer()
                        .invoke(acceptorBuilder)
                        .build();

        return getServerRSocketFactory()
                .addConnectionPlugin(setupInterceptor())
                .acceptor(() -> acceptor::accept)
                .transport(transport);
    }

    @NotNull
    private SetupInterceptor setupInterceptor() {
        return new SetupInterceptor(MimeType.INSTANCE);
    }
}
