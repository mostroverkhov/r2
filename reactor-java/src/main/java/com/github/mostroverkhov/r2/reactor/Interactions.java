package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Interactions {

  Mono<Void> fireAndForget(ServiceMethod call);

  Mono<?> requestResponse(ServiceMethod call);

  Flux<?> requestStream(ServiceMethod call);

  Flux<?> requestChannel(ServiceMethod call);

  Mono<Void> close();

  Mono<Void> onClose();
}
