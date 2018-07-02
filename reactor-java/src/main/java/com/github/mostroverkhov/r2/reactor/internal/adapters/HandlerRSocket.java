package com.github.mostroverkhov.r2.reactor.internal.adapters;

import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver;
import com.github.mostroverkhov.r2.core.internal.responder.TargetAction;
import com.github.mostroverkhov.r2.reactor.Interactions;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import com.github.mostroverkhov.r2.reactor.internal.interceptors.ResponderInteractions;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HandlerRSocket extends AbstractRSocket {
    private final ResponderTargetResolver targetResolver;
    private final Interactions interactions;

    public HandlerRSocket(ResponderTargetResolver targetResolver,
                          List<InteractionsInterceptor> interceptors) {
        this.targetResolver = targetResolver;
        this.interactions = createInteractions(interceptors);

    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        return callFireAndForget(payload);
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return callRequestResponse(payload);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        return callRequestStream(payload);
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return callRequestChannel(payloads);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Mono<Void> onClose() {
        return super.onClose();
    }

    private static Interactions createInteractions(
        List<InteractionsInterceptor> interceptors) {
        Interactions interactions = new ResponderInteractions();
        for (InteractionsInterceptor interceptor : interceptors) {
            interactions = interceptor.apply(interactions);
        }
        return interactions;
    }

    private Mono<Void> callFireAndForget(Payload payload) {
        TargetAction targetAction = resolveTarget(payload);
        return interactions.fireAndForget(targetAction);
    }

    private Mono<Payload> callRequestResponse(Payload payload) {
        TargetAction targetAction = resolveTarget(payload);
        return interactions.requestResponse(targetAction)
                .map(targetAction::encode)
                .map(this::payload);
    }

    private Flux<Payload> callRequestStream(Payload payload) {
        TargetAction targetAction = resolveTarget(payload);
        return interactions.requestStream(targetAction)
                .map(targetAction::encode)
                .map(this::payload);
    }

    private Flux<Payload> callRequestChannel(Publisher<Payload> arg) {
        return split(arg)
                .flatMap(headTail -> {
                    Payload headPayload = headTail.head();
                    Flux<Payload> tailPayload = headTail.tail();
                    TargetAction targetAction = resolveTarget(headPayload);
                    Flux<Object> payloadT = tailPayload
                            .map(p -> targetAction.decode(p.getData()));
                    TargetAction targetActionWithRequest = targetAction
                        .updateRequest(payloadT::startWith);

                    return interactions.requestChannel(targetActionWithRequest)
                            .map(targetAction::encode)
                            .map(this::payload);
                });
    }

    private TargetAction resolveTarget(Payload payload) {
        return targetResolver.resolve(payload.getData(), payload.getMetadata());
    }

    private Payload payload(ByteBuffer data) {
        return DefaultPayload.create(data, null);
    }

    private static Flux<Split> split(Publisher<Payload> p) {
        AtomicBoolean first = new AtomicBoolean(true);
        UnicastProcessor<Payload> rest = UnicastProcessor.create();
        UnicastProcessor<Split> channelArg = UnicastProcessor.create();

        return Flux.from(p)
                .doOnComplete(rest::onComplete)
                .doOnError(rest::onError)
                .flatMap(payload -> {
                    if (first.compareAndSet(true, false)) {
                        channelArg.onNext(new Split(payload, rest));
                        channelArg.onComplete();
                        return channelArg;
                    } else {
                        rest.onNext(payload);
                        return Flux.empty();
                    }
                });
    }

    private static class Split {
        private final Payload head;
        private final Flux<Payload> tail;

        public Split(Payload head, Flux<Payload> tail) {
            this.head = head;
            this.tail = tail;
        }

        public Payload head() {
            return head;
        }

        public Flux<Payload> tail() {
            return tail;
        }
    }
}
