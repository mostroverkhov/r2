package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor;
import com.github.mostroverkhov.r2.core.CoreServerAcceptorBuilder;
import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver;
import com.github.mostroverkhov.r2.reactor.internal.RequesterBuilder;
import com.github.mostroverkhov.r2.reactor.internal.adapters.HandlerRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiFunction;

public class ServerAcceptorBuilder extends CoreServerAcceptorBuilder<
    ConnectionSetupPayload,
    RSocket,
    Mono<RSocket>,
    ServerAcceptorBuilder> {

  private final List<InteractionsInterceptor> requesterInterceptors;
  private final List<InteractionsInterceptor> handlerInterceptors;

  public ServerAcceptorBuilder(List<InteractionsInterceptor> requesterInterceptors,
                               List<InteractionsInterceptor> handlerInterceptors) {
    this.requesterInterceptors = requesterInterceptors;
    this.handlerInterceptors = handlerInterceptors;
  }

  @NotNull
  @Override
  public ReactorServerAcceptor build() {
    return new ReactorServerAcceptor(
        (rSocket, metadata) -> {
          ResponderTargetResolver handlerTargetResolver =
              targetResolver(
                  rSocket,
                  metadata,
                  requesterRSocket -> new RequesterBuilder(
                      requesterRSocket,
                      requesterInterceptors));

          return new HandlerRSocket(
              handlerTargetResolver,
              handlerInterceptors);
        });
  }

  public static class ReactorServerAcceptor
      implements ServerAcceptor<ConnectionSetupPayload, RSocket, Mono<RSocket>> {

    private final BiFunction<RSocket, ByteBuffer, RSocket> handler;

    ReactorServerAcceptor(BiFunction<RSocket, ByteBuffer, RSocket> handler) {
      this.handler = handler;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket rSocket) {
      return Mono.just(handler.apply(rSocket, setup.getMetadata()));
    }
  }
}
