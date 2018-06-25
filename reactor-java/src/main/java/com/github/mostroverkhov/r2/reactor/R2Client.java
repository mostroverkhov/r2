package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.*;
import com.github.mostroverkhov.r2.core.internal.acceptor.ClientAcceptor;
import com.github.mostroverkhov.r2.core.internal.requester.ClientSetup;
import com.github.mostroverkhov.r2.core.internal.requester.SetupData;
import com.github.mostroverkhov.r2.reactor.internal.Assertions;
import com.github.mostroverkhov.r2.reactor.internal.RequesterBuilder;
import com.github.mostroverkhov.r2.reactor.monitoring.Monitored;
import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.rsocket.RSocket;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static io.rsocket.RSocketFactory.ClientRSocketFactory;

public class R2Client extends R2ClientFluentBuilder<
    ClientRSocketFactory,
    ClientAcceptorBuilder,
    ClientTransport,
    Mono<RequesterFactory>> implements Monitored<R2Client> {

  private final Assertions assertions = Assertions.forClient();
  private final InterceptorOptions interceptorOptions = new InterceptorOptions();
  private Function1<
      ? super ClientAcceptorBuilder,
      ? extends ClientAcceptorBuilder> configurer;
  private Metadata metadata;
  private ClientTransport clientTransport;
  private MonitoringSupport monitoringSupport;

  @NotNull
  @Override
  public R2Client metadata(@NotNull Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  @NotNull
  @Override
  public R2Client transport(ClientTransport clientTransport) {
    this.clientTransport = clientTransport;
    return this;
  }

  @NotNull
  @Override
  public R2Client configureAcceptor(
      @NotNull Function1<
          ? super ClientAcceptorBuilder,
          ? extends ClientAcceptorBuilder> f) {
    configurer = f;
    return this;
  }

  @NotNull
  public R2Client interceptors(
      @NotNull Consumer<InterceptorOptions> configurer) {
    configurer.accept(interceptorOptions);
    return this;
  }

  @Override
  public Mono<RequesterFactory> start() {
    assertState();

    @SuppressWarnings("ConstantConditions")
    ClientRSocketFactory rSocketFactory = withSetup(getRSocketFactory());
    ClientRSocketFactory monitoredRSocketFactory = withMonitoring(rSocketFactory);

    ClientAcceptorBuilder acceptorBuilder =
        new ClientAcceptorBuilder(
            interceptorOptions.requesters(),
            interceptorOptions.handlers());

    ClientAcceptorBuilder configuredBuilder =
        configurer
            .invoke(acceptorBuilder);
    ClientAcceptor<RSocket, RSocket> acceptor =
        configuredBuilder
            .build();

    DataCodec requesterCodec = configuredBuilder
        .codecs()
        .primary();

    Mono<RSocket> rSocket =
        monitoredRSocketFactory
            .acceptor(acceptor::accept)
            .transport(clientTransport)
            .start();

    Mono<RequesterFactory> requesterFactory =
        rSocket
            .map(rs -> new RequesterBuilder(rs, interceptorOptions.requesters()))
            .map(requesterBuilder -> requesterBuilder.codec(requesterCodec))
            .map(CoreRequesterBuilder::build);

    return requesterFactory;
  }

  @Override
  public R2Client monitor(MonitoringSupport monitoringSupport) {
    this.monitoringSupport = monitoringSupport;
    return this;
  }

  private ClientRSocketFactory withSetup(ClientRSocketFactory factory) {
    SetupData setup = ClientSetup.metaData(metadata);
    return factory
        .dataMimeType(setup.getDataType())
        .metadataMimeType(setup.getMetadataType())
        .setupPayload(
            DefaultPayload.create(
                setup.getData(),
                setup.getMetadata())
        );
  }

  private ClientRSocketFactory withMonitoring(ClientRSocketFactory factory) {
    if (monitoringSupport != null) {

      MonitoringSupport.RSocketMonitoring rSocketMonitoring =
          monitoringSupport.rSocket();
      factory.addClientPlugin(rSocketMonitoring.requester())
          .addServerPlugin(rSocketMonitoring.handler());

      MonitoringSupport.R2Monitoring r2Monitoring = monitoringSupport.r2();
      interceptors(opts ->
          opts.requester(r2Monitoring.requester())
              .handler(r2Monitoring.handler()));
    }
    return factory;
  }

  private void assertState() {
    assertions.assertArg(getRSocketFactory(), "RSocketFactory");
    assertions.assertArg(clientTransport, "Transport");
    assertions.assertArg(configurer, "AcceptorConfigurer");
    assertions.assertMonitoring(monitoringSupport);
  }
}
