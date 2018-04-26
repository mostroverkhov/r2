package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.core.Codecs;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.RequesterFactory;
import com.github.mostroverkhov.r2.core.Services;
import com.github.mostroverkhov.r2.example.Contract.Person;
import com.github.mostroverkhov.r2.example.Contract.PersonServiceHandler;
import com.github.mostroverkhov.r2.example.Contract.PersonsService;
import com.github.mostroverkhov.r2.example.Contract.RequestingPersonServiceHandler;
import com.github.mostroverkhov.r2.java.ClientAcceptorBuilder;
import com.github.mostroverkhov.r2.java.R2Client;
import com.github.mostroverkhov.r2.java.R2Server;
import com.github.mostroverkhov.r2.java.ServerAcceptorBuilder;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;


public class JavaClientServerExample {

  private static final int PORT = 8081;

  public static void main(String[] args) {
    /*Wraps Requester RSocket of client side of Connection*/
    Mono<PersonsService> service =
        clientRequester()
            .map(factory -> factory.create(PersonsService.class))
            .cache();

    Mono<NettyContextCloseable> started = new R2Server<NettyContextCloseable>()
        .connectWith(RSocketFactory.receive())
        /*Configure Requester and Responder sides of Server side of Connection*/
        .configureAcceptor(JavaClientServerExample::configureServer)
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
        /*Configure Requester and Responder sides of Client side of Connection*/
        .configureAcceptor(JavaClientServerExample::configureClient)
        .transport(TcpClientTransport.create(PORT))
        .start();
  }

  @NotNull
  private static ServerAcceptorBuilder configureServer(
      ServerAcceptorBuilder builder) {
    return builder
        /*Jackson Json codec. Also there can be cbor, protobuf etc*/
        .codecs(
            new Codecs()
                .add(new JacksonJsonDataCodec()))
        /*ConnectionContext represents Metadata(key -> value) set by
        Client (Connection initiator) as metadata.*/

        /*RequesterFactory uses first codec provided in Codecs*/
        .services((ctx, requesterFactory) ->
            new Services()
                .add(new RequestingPersonServiceHandler(
                    "server",
                    requesterFactory)));
  }

  @NotNull
  private static ClientAcceptorBuilder configureClient(
      ClientAcceptorBuilder b) {
    return b
        .codecs(new Codecs()
            .add(new JacksonJsonDataCodec()))
        .services(requesterFactory ->
            new Services()
                .add(new PersonServiceHandler("client")));
  }

  @NotNull
  private static Metadata metadata() {
    return new Metadata.Builder()
        .auth("secret".getBytes(Charsets.UTF_8))
        .build();
  }
}
