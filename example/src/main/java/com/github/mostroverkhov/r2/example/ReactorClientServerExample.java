package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.core.Codecs;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.Services;
import com.github.mostroverkhov.r2.example.ui.ControlUnitRenderer;
import com.github.mostroverkhov.r2.reactor.ClientAcceptorBuilder;
import com.github.mostroverkhov.r2.reactor.R2Client;
import com.github.mostroverkhov.r2.reactor.R2Server;
import com.github.mostroverkhov.r2.reactor.ServerAcceptorBuilder;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.NettyContextCloseable;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

import static com.github.mostroverkhov.r2.example.Contract.AssemblyLines;


public class ReactorClientServerExample {
  private static final int PORT = 0;
  private final ControlUnitRenderer renderer = new ControlUnitRenderer();

  public static void main(String[] args) {
    new ReactorClientServerExample().startAssemblyLineSystem();
  }

  void startAssemblyLineSystem() {

    Mono<NettyContextCloseable> serverStart = new R2Server<NettyContextCloseable>()
        .connectWith(RSocketFactory.receive())
        /*Configure Requester and Responder sides of Server side of Connection*/
        .configureAcceptor(this::configureServer)
        .transport(TcpServerTransport.create(PORT))
        .start();

    NettyContextCloseable serverStarted = serverStart.block();

    /*Wraps Requester RSocket of client side of Connection*/
    Mono<AssemblyLines.Svc> service = new R2Client()
        .connectWith(RSocketFactory.connect())
        /*Passed to Server (Connection Acceptor) as ConnectionContext*/
        .metadata(authenticate("total-secret"))
        /*Configure Requester and Responder sides of Client side of Connection*/
        .configureAcceptor(this::configureClient)
        .transport(TcpClientTransport.create(serverStarted.address()))
        .start()
        .map(factory -> factory.create(AssemblyLines.Svc.class))
        .cache();

    /*periodic requests*/
    Flux<AssemblyLines.Response> assemblyLineMonitoring =
        service.flatMapMany(svc ->
            svc.control(assembliesCommands()));

    assemblyLineMonitoring.subscribe(
        renderer::assemblyLineStateChanged,
        renderer::assemblyLineError);

    serverStarted.onClose().block();
  }

  @NotNull
  private ServerAcceptorBuilder configureServer(
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
                .add(new AssemblyLineHandler(ctx, requesterFactory)));
  }

  @NotNull
  private ClientAcceptorBuilder configureClient(
      ClientAcceptorBuilder b) {
    return b
        .codecs(new Codecs()
            .add(new JacksonJsonDataCodec()))
        .services(requesterFactory ->
            new Services()
                .add(new ControlUnitHandler()));
  }

  @NotNull
  private static Metadata authenticate(String token) {
    return new Metadata.Builder()
        .auth(token.getBytes())
        .build();
  }

  private static Flux<AssemblyLines.Request> assembliesCommands() {
    Random random = new Random();
    return Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(11))
        .map(__ ->
            new AssemblyLines.Request(
                random.nextInt(4)));
  }
}
