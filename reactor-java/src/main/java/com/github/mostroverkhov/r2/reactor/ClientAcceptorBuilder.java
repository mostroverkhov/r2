package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.CoreClientAcceptorBuilder;
import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver;
import com.github.mostroverkhov.r2.reactor.internal.RequesterBuilder;
import com.github.mostroverkhov.r2.reactor.internal.adapters.HandlerRSocket;
import io.rsocket.RSocket;

import java.util.List;
import java.util.function.Function;

public class ClientAcceptorBuilder extends CoreClientAcceptorBuilder<
    RSocket,
    RSocket,
    ClientAcceptorBuilder> {
  private final List<InteractionsInterceptor> handlerInterceptors;
  private final List<InteractionsInterceptor> requesterInterceptors;

  ClientAcceptorBuilder(List<InteractionsInterceptor> requesterInterceptors,
                        List<InteractionsInterceptor> handlerInterceptors) {
    this.handlerInterceptors = handlerInterceptors;
    this.requesterInterceptors = requesterInterceptors;
  }

  @Override
  public JavaClientAcceptor build() {
    return new JavaClientAcceptor(rSocket ->
    {
      ResponderTargetResolver responderTargetResolver =
          targetResolver(
              rSocket,
              requesterRSocket ->
                  new RequesterBuilder(
                      requesterRSocket,
                      requesterInterceptors));

      return new HandlerRSocket(
          responderTargetResolver,
          handlerInterceptors);
    });
  }

  static class JavaClientAcceptor implements ClientAcceptor<RSocket, RSocket> {
    private final Function<RSocket, RSocket> handler;

    JavaClientAcceptor(Function<RSocket, RSocket> handler) {
      this.handler = handler;
    }

    @Override
    public RSocket accept(RSocket requester) {
      return handler.apply(requester);
    }
  }
}
