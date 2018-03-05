package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.internal.MetadataCodec;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.r2.core.responder.Codecs;
import com.github.mostroverkhov.r2.core.responder.Services;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.util.PayloadImpl;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import static com.github.mostroverkhov.r2.java.JavaMocks.*;
import static org.junit.Assert.*;

public class JavaEndToEndTest {

    private PersonsService personsService;

    @Before
    public void setUp() throws Exception {

        Mono<RSocket> handlerRSocket = new JavaAcceptorBuilder()
                .codecs(new Codecs().add(new JacksonJsonDataCodec()))
                .services(ctx -> new Services().add(new PersonServiceHandler()))
                .build()
                .accept(mockSetupPayload());

        RequesterFactory requesterFactory = handlerRSocket
                .map(rs ->
                        new JavaRequesterBuilder(rs)
                                .codec(new JacksonJsonDataCodec())
                                .build())
                .block();

        personsService = requesterFactory.create(PersonsService.class);
    }

    @Test(timeout = 5_000)
    public void stream() throws Exception {
        Person expected = expectedPerson();
        List<Person> list = personsService.stream(expected)
                .collectList().block();
        assertEquals(1, list.size());
        Person actual = list.get(0);
        assertEquals(expected, actual);
    }

    @Test(timeout = 5_000)
    public void fireAndForget() throws Exception {
        Metadata md = new Metadata.Builder()
                .data("foo", "bar".getBytes(Charsets.UTF_8))
                .build();
        personsService.fnf(expectedPerson(), md).block();
    }

    @Test(timeout = 5_000)
    public void response() throws Exception {
        Metadata md = new Metadata.Builder()
                .data("foo", "bar".getBytes(Charsets.UTF_8))
                .build();
        Person expected = expectedPerson();
        Person actual = personsService.response(expected, md).block();
        assertEquals(expected, actual);
    }

    @Test(timeout = 5_000)
    public void channel() throws Exception {
        Person expected = expectedPerson();
        List<Person> list = personsService.channel(Flux.just(expected)).collectList().block();
        assertEquals(2, list.size());
        Person actual = list.get(0);
        assertEquals(expected, actual);
    }

    @Test(timeout = 5_000)
    public void emptyResponse() throws Exception {
        Person actual = personsService.responseEmpty().block();
        assertEquals(expectedPerson(), actual);
    }

    @Test(timeout = 5_000)
    public void onlyMetadataResponse() throws Exception {
        Metadata md = new Metadata.Builder()
                .data("foo", "bar".getBytes(Charsets.UTF_8))
                .build();

        Person actual = personsService.responseMetadata(md).block();
        assertEquals(expectedPerson(), actual);
    }

    @Test(timeout = 5_000, expected = IllegalArgumentException.class)
    public void noAnno() throws Exception {
        personsService.noAnno(expectedPerson()).collectList().block();
    }

    @Test(timeout = 5_000, expected = IllegalArgumentException.class)
    public void emptyAnno() {
        personsService.emptyAnno(expectedPerson()).collectList().block();
    }

    @NotNull
    private ConnectionSetupPayload mockSetupPayload() {
        Metadata md = new Metadata.Builder()
                .data("auth", Charsets.UTF_8.encode("secret"))
                .build();
        ByteBuffer encodedMd = new MetadataCodec().encode(md);
        return ConnectionSetupPayload
                .create("stub", "stub",
                        new PayloadImpl(ByteBuffer.allocate(0),
                                encodedMd));
    }

    @NotNull
    private Person expectedPerson() {
        return new Person("john", "doe");
    }
}
