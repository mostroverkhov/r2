package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter;
import com.github.mostroverkhov.r2.java.adapters.JavaRequesterAdapter;
import io.rsocket.AbstractRSocket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.function.Function;

public class RequesterAdapterTest {

    private CallAdapter adapter;

    @Before
    public void setUp() {
        adapter = new JavaRequesterAdapter(new AbstractRSocket() {
        });
    }


    @Test(expected = RuntimeException.class)
    public void wrongReturnType() throws Exception {
        Method obj = Stub.class.getMethod("obj");
        adapter.resolve(obj, new RuntimeException());
    }

    @Test
    public void monoType() throws Exception {
        assertError("mono", Mono.class, Mono::flux);
    }

    @Test
    public void fluxType() throws Exception {
        assertError("flux", Flux.class, flux -> flux);
    }

    private <T> void assertError(String method,
                                 Class<T> resolvedType,
                                 Function<T, Flux<?>> toFlux) throws Exception {
        Method obj = Stub.class.getMethod(method);
        RuntimeException ex = new RuntimeException();
        Object resolved = adapter.resolve(obj, ex);
        Assert.assertTrue(resolvedType.isAssignableFrom(resolved.getClass()));
        T cast = resolvedType.cast(resolved);
        Flux<?> compl = toFlux.apply(cast);
        StepVerifier.create(compl).expectNextCount(0)
                .expectError(ex.getClass())
                .verify(Duration.ofSeconds(1));
    }

    private interface Stub {
        Object obj();

        Mono<String> mono();

        Flux<String> flux();
    }
}
