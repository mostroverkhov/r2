package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor;
import com.github.mostroverkhov.r2.core.CoreServerAcceptorBuilder;
import com.github.mostroverkhov.r2.reactor.adapters.RSocketHandler;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

public class ServerAcceptorBuilder extends CoreServerAcceptorBuilder<
    ConnectionSetupPayload,
    RSocket,
    Mono<RSocket>,
    ServerAcceptorBuilder> {

  @NotNull
  @Override
  public JavaServerAcceptor build() {
    return new JavaServerAcceptor(
        (rSocket, metadata) ->
            new RSocketHandler(
                forTarget(rSocket, metadata, RequesterBuilder::new)));
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
