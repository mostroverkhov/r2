package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor;
import com.github.mostroverkhov.r2.core.ServerAcceptorBuilder;
import com.github.mostroverkhov.r2.java.adapters.JavaRSocketHandler;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

public class JavaServerAcceptorBuilder extends ServerAcceptorBuilder<
    ConnectionSetupPayload,
    RSocket,
    Mono<RSocket>,
    JavaServerAcceptorBuilder> {

  @NotNull
  @Override
  public JavaServerAcceptor build() {
    return new JavaServerAcceptor(
        (rSocket, metadata) ->
            new JavaRSocketHandler(
                forTarget(rSocket, metadata, JavaRequesterBuilder::new)));
  }

  public static class JavaServerAcceptor
      implements ServerAcceptor<ConnectionSetupPayload, RSocket, Mono<RSocket>> {

    private final BiFunction<RSocket, ByteBuffer, RSocket> handler;

    JavaServerAcceptor(BiFunction<RSocket, ByteBuffer, RSocket> handler) {
      this.handler = handler;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket rSocket) {
      return Mono.just(handler.apply(rSocket, setup.getMetadata()));
    }
  }
}
