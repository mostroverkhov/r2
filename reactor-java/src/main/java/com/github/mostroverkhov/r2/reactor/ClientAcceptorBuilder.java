package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.CoreClientAcceptorBuilder;
import com.github.mostroverkhov.r2.reactor.adapters.RSocketHandler;
import io.rsocket.RSocket;

import java.util.function.Function;

public class ClientAcceptorBuilder extends CoreClientAcceptorBuilder<
    RSocket,
    RSocket,
    ClientAcceptorBuilder> {

  @Override
  public JavaClientAcceptor build() {
    return new JavaClientAcceptor(rSocket ->
        new RSocketHandler(
            forTarget(rSocket, RequesterBuilder::new)));
  }

  static class JavaClientAcceptor implements ClientAcceptor<RSocket, RSocket> {
    private final Function<RSocket,  RSocket> handler;

    JavaClientAcceptor(Function<RSocket, RSocket> handler) {
      this.handler = handler;
    }

    @Override
    public RSocket accept(RSocket requester) {
      return handler.apply(requester);
    }
  }
}
