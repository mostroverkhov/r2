package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.DataCodec;
import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.R2ClientFluentBuilder;
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.internal.requester.ClientSetup;
import com.github.mostroverkhov.r2.core.internal.requester.SetupData;
import com.github.mostroverkhov.r2.core.RequesterBuilder;
import com.github.mostroverkhov.r2.core.RequesterFactory;
import io.rsocket.RSocket;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.PayloadImpl;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static io.rsocket.RSocketFactory.ClientRSocketFactory;

public class R2Client extends R2ClientFluentBuilder<
    ClientRSocketFactory,
    JavaClientAcceptorBuilder,
    ClientTransport,
    Mono<RequesterFactory>> {

  private Function1<
      ? super JavaClientAcceptorBuilder,
      ? extends JavaClientAcceptorBuilder> configurer;
  private Metadata metadata;
  private ClientTransport clientTransport;

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      JavaClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> metadata(@NotNull Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      JavaClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> transport(ClientTransport clientTransport) {
    this.clientTransport = clientTransport;
    return this;
  }

  @NotNull
  @Override
  public R2ClientFluentBuilder<
      ClientRSocketFactory,
      JavaClientAcceptorBuilder,
      ClientTransport,
      Mono<RequesterFactory>> configureAcceptor(
      @NotNull Function1<
          ? super JavaClientAcceptorBuilder,
          ? extends JavaClientAcceptorBuilder> f) {
    configurer = f;
    return this;
  }

  @Override
  public Mono<RequesterFactory> start() {
    assertState();

    JavaClientAcceptorBuilder acceptorBuilder =
        new JavaClientAcceptorBuilder();

    JavaClientAcceptorBuilder configuredBuilder =
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
            .map(JavaRequesterBuilder::new)
            .map(requesterBuilder -> requesterBuilder.codec(requesterCodec))
            .map(RequesterBuilder::build);

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
