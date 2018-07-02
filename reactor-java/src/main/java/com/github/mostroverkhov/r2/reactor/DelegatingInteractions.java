package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DelegatingInteractions implements Interactions {
  private final Interactions source;

  public DelegatingInteractions(Interactions source) {
    this.source = source;
  }

  @Override
  public Mono<Void> fireAndForget(ServiceMethod call) {
    return source.fireAndForget(call);
  }

  @Override
  public Mono<?> requestResponse(ServiceMethod call) {
    return source.requestResponse(call);
  }

  @Override
  public Flux<?> requestStream(ServiceMethod call) {
    return source.requestStream(call);
  }

  @Override
  public Flux<?> requestChannel(ServiceMethod call) {
    return source.requestChannel(call);
  }

  @Override
  public Mono<Void> close() {
    return source.close();
  }

  @Override
  public Mono<Void> onClose() {
    return source.onClose();
  }
}
