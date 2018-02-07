package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.requester.*;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.PayloadImpl;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

class JavaRequesterAdapter implements CallAdapter {
    private final RSocket rSocket;

    public JavaRequesterAdapter(RSocket rSocket) {
        this.rSocket = rSocket;
    }

    @NotNull
    @Override
    public Object adapt(@NotNull Call call) {
        Interaction interaction = call.getInteraction();
        switch (interaction) {
            case CHANNEL:
                return Flux.defer(() ->
                        rSocket.requestChannel(encodePublisher(call))
                                .map(payload -> decode(call, payload)));

            case RESPONSE:
                return Mono.defer(() ->
                        rSocket.requestResponse(encode(call))
                                .map(payload -> decode(call, payload)));

            case STREAM:
                return Flux.defer(
                        () -> rSocket.requestStream(encode(call))
                                .map(payload -> decode(call, payload)));

            case FNF:
                return Mono.defer(() ->
                        rSocket.fireAndForget(encode(call)));

            case CLOSE:
                return rSocket.close();

            case ONCLOSE:
                return rSocket.onClose();

            default:
                throw new IllegalArgumentException("Unsupported interaction: " + interaction);
        }
    }

    private Payload encode(Call call) {
        RequestCall requestCall = cast(call);
        return new PayloadImpl(
                requestCall.encodeData(requestCall.getArgs().getData()),
                requestCall.encodeMetadata());
    }

    @SuppressWarnings("ConstantConditions")
    private Publisher<Payload> encodePublisher(Call call) {
        RequestCall requestCal = cast(call);
        final AtomicBoolean first = new AtomicBoolean(true);
        /*suppressed as non-nullness for Request-Channel is verified by ActionArgs Builder */
        return Flux.from(((Publisher<?>) requestCal.getArgs().getData()))
                .map(t -> {
                    ByteBuffer metadata =
                            first.compareAndSet(true, false)
                                    ? requestCal.encodeMetadata()
                                    : null;
                    return new PayloadImpl(requestCal.encodeData(t), metadata);
                });
    }

    private Object decode(Call call, Payload payload) {
        return cast(call).decodeData(payload.getData());
    }

    private RequestCall cast(Call call) {
        return (RequestCall) call;
    }

    @NotNull
    @Override
    public Object resolve(@NotNull Method action, @NotNull RuntimeException err) {
        Class<?> returnType = action.getReturnType();
        if (Mono.class.equals(returnType)) {
            return Mono.error(err);
        } else if (Flux.class.equals(returnType)) {
            return Flux.error(err);
        } else {
            throw err;
        }
    }
}
