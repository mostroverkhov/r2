package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonDataCodec;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import static com.github.mostroverkhov.r2.java.JavaMocks.*;

public class JavaEndToEndTest {

    private PersonsService personsService;

    @Before
    public void setUp() throws Exception {

        Mono<RSocket> handlerRSocket = new JavaAcceptorBuilder()
                .codecs(new Codecs().add(new JacksonDataCodec()))
                .services(ctx -> new Services().add(new PersonServiceHandler()))
                .build()
                .accept(mockSetupPayload());

        RequesterFactory requesterFactory = handlerRSocket
                .map(rs ->
                        new JavaRequesterBuilder(rs)
                                .codec(new JacksonDataCodec())
                                .build())
                .block();

        personsService = requesterFactory.create(PersonsService.class);
    }

    @Test(timeout = 5_000)
    public void stream() throws Exception {
        List<Person> list = personsService.stream(new Person("john", "doe"))
                .collectList().block();
        Assert.assertEquals(1, list.size());
        Person person = list.get(0);
        Assert.assertEquals("john", person.getName());
        Assert.assertEquals("doe", person.getSurname());
    }

    @Test(timeout = 5_000)
    public void fireAndForget() throws Exception {
        personsService.fnf(new Person("john", "doe")).block();
    }

    @Test(timeout = 5_000)
    public void response() throws Exception {
        Person person = personsService.response(new Person("john", "doe")).block();
        Assert.assertEquals("john", person.getName());
        Assert.assertEquals("doe", person.getSurname());
    }

    @Test(timeout = 5_000)
    public void channel() throws Exception {
        List<Person> list = personsService.channel(Flux.just(new Person("john", "doe"))).collectList().block();
        Assert.assertEquals(2, list.size());
        Person person = list.get(0);
        Assert.assertEquals("john", person.getName());
        Assert.assertEquals("doe", person.getSurname());
    }

    @Test(timeout = 5_000, expected = IllegalArgumentException.class)
    public void noAnno() throws Exception {
        personsService.noAnno(new Person("john", "doe")).collectList().block();
    }

    @Test(timeout = 5_000, expected = IllegalArgumentException.class)
    public void emptyAnno() {
        personsService.emptyAnno(new Person("john", "doe")).collectList().block();
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
}
