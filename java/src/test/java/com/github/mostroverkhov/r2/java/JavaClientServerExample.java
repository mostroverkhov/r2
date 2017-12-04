package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonDataCodec;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.internal.responder.RequestAcceptor;
import com.github.mostroverkhov.r2.core.requester.RequesterFactory;
import com.github.mostroverkhov.r2.core.responder.Codecs;
import com.github.mostroverkhov.r2.core.responder.Services;
import com.github.mostroverkhov.r2.java.JavaMocks.Person;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import kotlin.Pair;
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
                clientRequester().map(f -> f.create(PersonsService.class));

        /*Corresponds Responder RSocket of server side of Connection. Requester RSocket is not exposed yet*/
        RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> serverAcceptor = serverAcceptor();

        Pair<NettyContextCloseable, Flux<Person>> pair = RSocketFactory
                .receive()
                .acceptor(() ->
                        (setup, sendRSocket) -> serverAcceptor.accept(setup)
                ).transport(TcpServerTransport.create(PORT))
                .start()
                .map(closeable -> {
                    Flux<Person> persons = Flux.interval(Duration.ofSeconds(1))
                            .flatMap(__ -> service
                                    .flatMapMany(svc ->
                                            svc.channel(
                                                    Flux.just(new Person("john", "doe")))
                                    )
                            );
                    return new Pair<>(closeable, persons);
                }).block();

        pair.getSecond().subscribe(System.out::println);
        pair.getFirst().onClose().block();

    }

    private static Mono<RequesterFactory> clientRequester() {
        return new R2Client()
                .connectWith(clientRSocketFactory())
                /*Passed to Server (Connection Acceptor) as ConnectionContext*/
                .metadata(metadata())
                .transport(TcpClientTransport.create(PORT))
                .configureRequester(b -> b.codec(new JacksonDataCodec()));
    }

    @NotNull
    private static RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> serverAcceptor() {
        return new JavaAcceptorBuilder()
                .codecs(new Codecs().add(new JacksonDataCodec()))
                /*ConnectionContext represents Metadata(key -> value) set by Client (Connection initiator)
                as metadata*/
                .services(ctx -> new Services().add(new PersonServiceHandler()))
                .build();
    }

    @NotNull
    private static RSocketFactory.ClientRSocketFactory clientRSocketFactory() {
        return RSocketFactory
                .connect();
    }

    @NotNull
    private static Metadata metadata() {
        return new Metadata.Builder()
                .auth("secret".getBytes(Charsets.UTF_8))
                .build();
    }
}
