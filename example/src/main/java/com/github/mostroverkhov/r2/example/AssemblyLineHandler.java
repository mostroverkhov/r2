package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.core.ConnectionContext;
import com.github.mostroverkhov.r2.core.RequesterFactory;
import com.github.mostroverkhov.r2.example.ui.AssemblyLineRenderer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.util.Optional;

import static com.github.mostroverkhov.r2.example.Contract.AssemblyLines;
import static com.github.mostroverkhov.r2.example.Contract.AssemblyLines.Request;
import static com.github.mostroverkhov.r2.example.Contract.AssemblyLines.Response;
import static com.github.mostroverkhov.r2.example.Contract.ControlUnit;

public class AssemblyLineHandler implements AssemblyLines.Svc {

  private final ControlUnit.Svc controlUnitSvc;
  private final AssemblyLineModel assemblyLineModel = new AssemblyLineModel();
  private final AssemblyLineRenderer renderer = new AssemblyLineRenderer();

  public AssemblyLineHandler(Optional<byte[]> auth,
                             ControlUnit.Svc controlUnitSvc) {
    this.controlUnitSvc = controlUnitSvc;
    auth.ifPresent(renderer::authChanged);
    pollControlUnitPower();
  }

  public AssemblyLineHandler(ConnectionContext ctx,
                             RequesterFactory requesterFactory) {
    this(Optional.ofNullable(ctx.auth()), controlUnitSvc(requesterFactory));
  }

  @Override
  public Flux<Response> control(Flux<Request> command) {
    return Flux.defer(
        () -> {
          ReplayProcessor<Request> commands = ReplayProcessor.cacheLast();
          command.doOnNext(renderer::assemblyLinesChanged)
              .subscribe(commands);
          return commands
              .next()
              .thenMany(
                  Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(3))
                      .flatMap(__ ->
                          commands
                              .next()
                              .map(Request::getActiveAssemblies)
                              .map(assemblyLineModel::monitoringResponse)));
        });
  }

  private static ControlUnit.Svc controlUnitSvc(RequesterFactory requesterFactory) {
    return requesterFactory.create(ControlUnit.Svc.class);
  }

  /*periodically ask peer about available power*/
  private void pollControlUnitPower() {
    Flux.interval(Duration.ofSeconds(7))
        .flatMap(__ -> controlUnitSvc.power())
        .subscribe(renderer::powerChanged, renderer::powerError);
  }
}
