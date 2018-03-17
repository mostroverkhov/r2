package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptor;
import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptorBuilder;
import com.github.mostroverkhov.rsocket.ConnectionSetupPayload;
import com.github.mostroverkhov.rsocket.RSocket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class JavaAcceptorBuilder extends RequestAcceptorBuilder<
        ConnectionSetupPayload,
        Mono<RSocket>,
        JavaAcceptorBuilder> {

    JavaAcceptorBuilder() {
    }

    @NotNull
    @Override
    public JavaRequestAcceptor build() {
        return new JavaRequestAcceptor(metadata ->
                new JavaRSocketHandler(targetResolver(metadata)));
    }

    static class JavaRequestAcceptor implements RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> {

        private final Function<ByteBuffer, RSocket> handlerFactory;

        JavaRequestAcceptor(Function<ByteBuffer, RSocket> handlerFactory) {
            this.handlerFactory = handlerFactory;
        }

        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setup) {
            return Mono.just(handlerFactory.apply(setup.getMetadata()));
        }
    }
}
