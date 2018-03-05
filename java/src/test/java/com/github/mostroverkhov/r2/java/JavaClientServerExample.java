package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.r2.core.responder.Codecs;
import com.github.mostroverkhov.r2.core.responder.Services;
import com.github.mostroverkhov.r2.java.JavaMocks.Person;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.github.mostroverkhov.r2.java.JavaMocks.*;
import static com.github.mostroverkhov.r2.java.JavaMocks.PersonsService;

public class JavaClientServerExample {

    private static final int PORT = 8081;

    public static void main(String[] args) {
        /*Wraps Requester RSocket of client side of Connection*/
        Mono<PersonsService> service =
                clientRequester().map(factory -> factory.create(PersonsService.class))
                        .cache();

        Mono<NettyContextCloseable> started = new R2Server<NettyContextCloseable>()
                .connectWith(RSocketFactory.receive())
                /*Configure Responder RSocket (acceptor) of server side of Connection.
                  Requester RSocket is not exposed yet*/
                .configureAcceptor(JavaClientServerExample::configureAcceptor)
                .transport(TcpServerTransport.create(PORT))
                .start();

        /*periodic requests*/
        Flux<Person> persons = Flux.interval(Duration.ofSeconds(1))
                .flatMap(__ -> service
                        .flatMapMany(svc ->
                                svc.channel(
                                        Flux.just(new Person("john", "doe")))
                        )
                );
        NettyContextCloseable closeable = started.block();
        persons.subscribe(System.out::println, System.out::println);
        closeable.onClose().block();

    }

    private static Mono<RequesterFactory> clientRequester() {
        return new R2Client()
                .connectWith(RSocketFactory.connect())
                /*Passed to Server (Connection Acceptor) as ConnectionContext*/
                .metadata(metadata())
                .configureRequester(b -> b.codec(new JacksonJsonDataCodec()))
                .transport(TcpClientTransport.create(PORT))
                .start();
    }

    @NotNull
    private static JavaAcceptorBuilder configureAcceptor(JavaAcceptorBuilder builder) {
        return builder
                /*Jackson codec. Also there can be cbor, protobuf etc*/
                .codecs(new Codecs().add(new JacksonJsonDataCodec()))
                /*ConnectionContext represents Metadata(key -> value) set by Client (Connection initiator)
                as metadata*/
                .services(ctx -> new Services().add(new PersonServiceHandler()));
    }

    @NotNull
    private static Metadata metadata() {
        return new Metadata.Builder()
                .auth("secret".getBytes(Charsets.UTF_8))
                .build();
    }
}
