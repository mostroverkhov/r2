package com.github.mostroverkhov.r2.reactor.internal.interceptors;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import com.github.mostroverkhov.r2.core.internal.responder.TargetAction;
import com.github.mostroverkhov.r2.reactor.Interactions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

public class ResponderInteractions implements Interactions {

  private final MonoProcessor<Void> onClose = MonoProcessor.create();

  @Override
  public Mono<Void> fireAndForget(ServiceMethod call) {
    return invoke(call);
  }

  @Override
  public Mono<?> requestResponse(ServiceMethod call) {
    return invoke(call);
  }

  @Override
  public Flux<?> requestStream(ServiceMethod call) {
    return invoke(call);
  }

  @Override
  public Flux<?> requestChannel(ServiceMethod call) {
    return invoke(call);
  }

  @Override
  public Mono<Void> close() {
    return onClose.doOnSubscribe(s -> onClose.onComplete());
  }

  @Override
  public Mono<Void> onClose() {
    return onClose;
  }

  private <T> T invoke(ServiceMethod serviceMethod) {
    if (serviceMethod instanceof TargetAction) {
      TargetAction targetAction = ((TargetAction) serviceMethod);
      return targetAction.invoke();
    } else {
      throw new IllegalArgumentException("unexpected ServiceMethod type: InteractionInterceptors are " +
          "expected to pass upstream ServiceMethod");
    }
  }
}
