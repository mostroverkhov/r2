package com.github.mostroverkhov.r2.reactor.internal.interceptors;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCall;
import com.github.mostroverkhov.r2.reactor.Interactions;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequesterInteractions implements Interactions {
  private final RSocket rSocket;

  public RequesterInteractions(RSocket rSocket) {
    this.rSocket = rSocket;
  }

  @Override
  public Mono<Void> fireAndForget(ServiceMethod call) {
    return rSocket.fireAndForget(encode(call));
  }

  @Override
  public Mono<?> requestResponse(ServiceMethod call) {
    return rSocket.requestResponse(encode(call))
        .map(payload -> decode(call, payload));
  }

  @Override
  public Flux<?> requestStream(ServiceMethod call) {
    return rSocket.requestStream(encode(call))
        .map(payload -> decode(call, payload));
  }

  @Override
  public Flux<?> requestChannel(ServiceMethod call) {
    return rSocket.requestChannel(encodePublisher(call))
        .map(payload -> decode(call, payload));
  }

  @Override
  public Mono<Void> close() {
    return Mono.fromRunnable(rSocket::dispose);
  }

  @Override
  public Mono<Void> onClose() {
    return rSocket.onClose();
  }

  private Payload encode(ServiceMethod call) {
    RequesterCall requesterCall = cast(call);
    return DefaultPayload.create(
        requesterCall.encodeData(requesterCall.params().getData()),
        requesterCall.encodeMetadata());
  }

  @SuppressWarnings("ConstantConditions")
  private Publisher<Payload> encodePublisher(ServiceMethod call) {
    RequesterCall requesterCall = cast(call);
    final AtomicBoolean first = new AtomicBoolean(true);
    /*suppressed as non-nullness for Request-Channel is verified by CallParams Builder */
    return Flux.from(((Publisher<?>) requesterCall.params().getData()))
        .map(t -> {
          ByteBuffer metadata =
              first.compareAndSet(true, false)
                  ? requesterCall.encodeMetadata()
                  : null;
          return DefaultPayload.create(requesterCall.encodeData(t), metadata);
        });
  }

  private Object decode(ServiceMethod call, Payload payload) {
    return cast(call).decodeData(payload.getData());
  }

  private RequesterCall cast(ServiceMethod call) {
    return (RequesterCall) call;
  }
}
