package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.*;
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.internal.requester.ClientSetup;
import com.github.mostroverkhov.r2.core.internal.requester.SetupData;
import io.rsocket.RSocket;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.PayloadImpl;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static io.rsocket.RSocketFactory.ClientRSocketFactory;

public class R2Client extends R2ClientFluentBuilder<
    ClientRSocketFactory,
    ClientAcceptorBuilder,
    ClientTransport,
    Mono<RequesterFactory>> {

  private Function1<
      ? super ClientAcceptorBuilder,
      ? extends ClientAcceptorBuilder> configurer;
  private Metadata metadata;
  private ClientTransport clientTransport;

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      ClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> metadata(@NotNull Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      ClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> transport(ClientTransport clientTransport) {
    this.clientTransport = clientTransport;
    return this;
  }

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      ClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> configureAcceptor(
      @NotNull Function1<
          ? super ClientAcceptorBuilder,
          ? extends ClientAcceptorBuilder> f) {
    configurer = f;
    return this;
  }

  @Override
  public Mono<RequesterFactory> start() {
    assertState();

    ClientAcceptorBuilder acceptorBuilder =
        new ClientAcceptorBuilder();

    ClientAcceptorBuilder configuredBuilder =
        configurer
            .invoke(acceptorBuilder);
    ClientAcceptor<RSocket, RSocket> acceptor =
        configuredBuilder
            .build();

    DataCodec requesterCodec = configuredBuilder
        .codecs()
        .primary();

    @SuppressWarnings("ConstantConditions")
    ClientRSocketFactory rSocketFactory = withSetup(getRSocketFactory());

    Mono<RSocket> rSocket =
        rSocketFactory
            .acceptor(acceptor::accept)
            .transport(clientTransport)
            .start();

    Mono<RequesterFactory> requesterFactory =
        rSocket
            .map(RequesterBuilder::new)
            .map(requesterBuilder -> requesterBuilder.codec(requesterCodec))
            .map(CoreRequesterBuilder::build);

    return requesterFactory;
  }

  private ClientRSocketFactory withSetup(ClientRSocketFactory factory) {
    SetupData setup = ClientSetup.metaData(metadata);
    return factory
        .dataMimeType(setup.getDataType())
        .metadataMimeType(setup.getMetadataType())
        .setupPayload(
            new PayloadImpl(
                setup.getData(),
                setup.getMetadata())
        );
  }

  private void assertState() {
    assertArg(getRSocketFactory(), "ClientRSocketFactory");
    assertArg(clientTransport, "ClientTransport");
    assertArg(configurer, "RequesterConfigurer");
  }

  private static void assertArg(Object arg, String name) {
    if (arg == null) {
      throw new IllegalArgumentException(name + " was not set");
    }
  }
}
