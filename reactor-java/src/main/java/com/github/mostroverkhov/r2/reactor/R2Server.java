package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.R2ServerFluentBuilder;
import com.github.mostroverkhov.r2.core.internal.MimeType;
import com.github.mostroverkhov.r2.core.internal.acceptor.ServerAcceptor;
import com.github.mostroverkhov.r2.reactor.internal.Assertions;
import com.github.mostroverkhov.r2.reactor.internal.SetupInterceptor;
import com.github.mostroverkhov.r2.reactor.monitoring.Monitored;
import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.rsocket.Closeable;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory.ServerRSocketFactory;
import io.rsocket.RSocketFactory.Start;
import io.rsocket.transport.ServerTransport;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport.*;

public class R2Server<T extends Closeable> extends R2ServerFluentBuilder<
        ServerRSocketFactory,
    ServerAcceptorBuilder,
        ServerTransport<T>,
        Start<T>> implements Monitored<R2Server<T>> {

    private final InterceptorOptions interceptorOptions = new InterceptorOptions();
    private final Assertions assertions = Assertions.forServer();
    private MonitoringSupport monitoringSupport;

    @SuppressWarnings("ConstantConditions")
    @Override
    public Start<T> transport(ServerTransport<T> transport) {
        assertState();
        ServerRSocketFactory rSocketFactory = withSetup(getRSocketFactory());
        ServerRSocketFactory monitoredRSocketFactory = withMonitoring(rSocketFactory);

        ServerAcceptorBuilder acceptorBuilder = new ServerAcceptorBuilder(
            interceptorOptions.requesters(),
            interceptorOptions.requesters());

        ServerAcceptor<ConnectionSetupPayload, RSocket,Mono<RSocket>> acceptor =
                getAcceptorConfigurer()
                        .invoke(acceptorBuilder)
                        .build();

        return monitoredRSocketFactory
                .acceptor(acceptor::accept)
                .transport(transport);
    }

    @Override
    public R2Server<T> monitor(MonitoringSupport monitoringSupport) {
        this.monitoringSupport = monitoringSupport;
        return this;
    }

    @NotNull
    public R2Server<T> interceptors(
        @NotNull Consumer<InterceptorOptions> configurer) {
        configurer.accept(interceptorOptions);
        return this;
    }

    private void assertState() {
        assertions.assertArg(getRSocketFactory(), "RSocketFactory");
        assertions.assertArg(getAcceptorConfigurer(), "AcceptorConfigurer");
        assertions.assertMonitoring(monitoringSupport);
    }

    private ServerRSocketFactory withSetup(ServerRSocketFactory factory) {
        return factory
                .addConnectionPlugin(setupInterceptor());
    }

    private ServerRSocketFactory withMonitoring(ServerRSocketFactory factory) {
        if (monitoringSupport != null) {
            RSocketMonitoring rSocketMonitoring = monitoringSupport.rSocket();
            factory.addConnectionPlugin(rSocketMonitoring.connection())
                .addClientPlugin(rSocketMonitoring.requester())
                .addServerPlugin(rSocketMonitoring.handler());

            R2Monitoring r2Monitoring = monitoringSupport.r2();
            interceptors(opts ->
                opts.requester(r2Monitoring.requester())
                    .handler(r2Monitoring.handler()));
        }
        return factory;
    }

    @NotNull
    private SetupInterceptor setupInterceptor() {
        return new SetupInterceptor(MimeType.INSTANCE);
    }
}
