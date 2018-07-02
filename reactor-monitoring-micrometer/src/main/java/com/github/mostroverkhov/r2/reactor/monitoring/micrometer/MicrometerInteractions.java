package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import com.github.mostroverkhov.r2.reactor.DelegatingInteractions;
import com.github.mostroverkhov.r2.reactor.Interactions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.function.Function;

class MicrometerInteractions extends DelegatingInteractions {

  private final MeterRegistry meterRegistry;
  private final Tag[] tags;

  public MicrometerInteractions(Interactions source,
                                MeterRegistry meterRegistry,
                                Tag[] tags) {
    super(source);
    this.meterRegistry = meterRegistry;
    this.tags = tags;
  }

  @Override
  public Mono<Void> fireAndForget(ServiceMethod call) {
    return super.fireAndForget(call)
        .compose(countMono(call));
  }

  @Override
  public Mono<?> requestResponse(ServiceMethod call) {
    return super.requestResponse(call)
        .compose(countMono(call));
  }

  @Override
  public Flux<?> requestStream(ServiceMethod call) {
    return super.requestStream(call)
        .compose(countFlux(call));
  }

  @Override
  public Flux<?> requestChannel(ServiceMethod call) {
    return super.requestChannel(call)
        .compose(countFlux(call));
  }

  private <T> Function<Mono<T>, Mono<T>> countMono(ServiceMethod call) {
    return source -> source
        .doOnSubscribe(s ->
            serviceMethodCounter(
                call,
                Contract.Call.EventType.START)
                .increment())
        .doFinally(signalType ->
            serviceMethodCounter(
                call,
                eventType(signalType))
                .increment());
  }

  private <T> Function<Flux<T>, Flux<T>> countFlux(ServiceMethod call) {
    return source -> source
        .doOnSubscribe(s ->
            serviceMethodCounter(
                call,
                Contract.Call.EventType.START)
                .increment())
        .doFinally(signalType ->
            serviceMethodCounter(
                call,
                eventType(signalType))
                .increment());
  }

  private String eventType(SignalType signalType) {
    switch (signalType) {
      case CANCEL:
        return Contract.Call.EventType.CANCEL;
      case ON_ERROR:
        return Contract.Call.EventType.ERROR;
      case ON_COMPLETE:
        return Contract.Call.EventType.COMPLETE;
      default:
        return signalType.name();
    }
  }

  @NotNull
  private Counter serviceMethodCounter(ServiceMethod call,
                                       String signalType) {
    return meterRegistry
        .counter(Contract.CALL,
            Tags.of(tags)
                .and(Contract.Call.SERVICE, call.getService())
                .and(Contract.Call.METHOD, call.getMethod())
                .and(Contract.Call.EVENT_TYPE, signalType));
  }

  private static class Contract {

    private static final String CALL = "rsocket.r2.call";

    private static class Call {
      private static final String SERVICE = "service";
      private static final String METHOD = "method";
      private static final String EVENT_TYPE = "event.type";

      private static class EventType {
        private static final String START = "start";
        private static final String COMPLETE = "complete";
        private static final String ERROR = "error";
        private static final String CANCEL = "cancel";
      }
    }
  }
}
