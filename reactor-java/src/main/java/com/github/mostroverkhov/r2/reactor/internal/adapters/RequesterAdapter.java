package com.github.mostroverkhov.r2.reactor.internal.adapters;

import com.github.mostroverkhov.r2.core.internal.requester.Call;
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter;
import com.github.mostroverkhov.r2.core.internal.requester.Interaction;
import com.github.mostroverkhov.r2.reactor.Interactions;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import com.github.mostroverkhov.r2.reactor.internal.interceptors.RequesterInteractions;
import io.rsocket.RSocket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

public class RequesterAdapter implements CallAdapter {
    private final Interactions interactions;

    public RequesterAdapter(RSocket rSocket,
                            List<InteractionsInterceptor> interceptors) {
        this.interactions = createInteractions(rSocket, interceptors);
    }

    @NotNull
    @Override
    public Object adapt(@NotNull Call call) {
        Interaction interaction = call.getInteraction();
        switch (interaction) {
            case CHANNEL:
                return Flux.defer(() -> interactions.requestChannel(call));

            case RESPONSE:
                return Mono.defer(() -> interactions.requestResponse(call));

            case STREAM:
                return Flux.defer(() -> interactions.requestStream(call));

            case FNF:
                return Mono.defer(() -> interactions.fireAndForget(call));

            case CLOSE:
                return interactions.close();

            case ONCLOSE:
                return interactions.onClose();

            default:
                throw new IllegalArgumentException(
                    "Unsupported interaction: " + interaction);
        }
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

    private static Interactions createInteractions(
        RSocket rSocket,
        List<InteractionsInterceptor> interceptors) {
        Interactions interactions = new RequesterInteractions(rSocket);
        for (InteractionsInterceptor interceptor : interceptors) {
            interactions = interceptor.apply(interactions);
        }
        return interactions;
    }
}
