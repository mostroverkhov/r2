package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.ClientAcceptorBuilder;
import com.github.mostroverkhov.r2.java.adapters.JavaRSocketHandler;
import io.rsocket.RSocket;

import java.util.function.Function;

public class JavaClientAcceptorBuilder extends ClientAcceptorBuilder<
    RSocket,
    RSocket,
    JavaClientAcceptorBuilder> {

  @Override
  public JavaClientAcceptor build() {
    return new JavaClientAcceptor(rSocket ->
        new JavaRSocketHandler(
            forTarget(rSocket, JavaRequesterBuilder::new)));
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
