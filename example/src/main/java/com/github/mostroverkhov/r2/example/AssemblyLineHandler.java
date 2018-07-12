package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.core.ConnectionContext;
import com.github.mostroverkhov.r2.core.RequesterFactory;
import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesRequest;
import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesResponse;
import com.github.mostroverkhov.r2.example.contract.services.reactor.AssemblyLinesService;
import com.github.mostroverkhov.r2.example.contract.services.reactor.ControlUnitService;
import com.github.mostroverkhov.r2.example.ui.AssemblyLineRenderer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.time.Duration;
import java.util.Optional;

public class AssemblyLineHandler implements AssemblyLinesService {

  private final ControlUnitService controlUnitSvc;
  private final AssemblyLineModel assemblyLineModel = new AssemblyLineModel();
  private final AssemblyLineRenderer renderer = new AssemblyLineRenderer();

  public AssemblyLineHandler(Optional<byte[]> auth,
                             ControlUnitService controlUnitSvc) {
    this.controlUnitSvc = controlUnitSvc;
    auth.ifPresent(renderer::authChanged);
    pollControlUnitPower();
  }

  public AssemblyLineHandler(ConnectionContext ctx,
                             RequesterFactory requesterFactory) {
    this(Optional.ofNullable(ctx.auth()), controlUnitSvc(requesterFactory));
  }

  @Override
  public Flux<AssemblyLinesResponse> control(Flux<AssemblyLinesRequest> command) {
    return Flux.defer(
        () -> {
          ReplayProcessor<AssemblyLinesRequest> commands = ReplayProcessor.cacheLast();
          command.doOnNext(renderer::assemblyLinesChanged)
              .subscribe(commands);
          return commands
              .next()
              .thenMany(
                  Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(3))
                      .flatMap(__ ->
                          commands
                              .next()
                              .map(AssemblyLinesRequest::getActiveAssemblies)
                              .map(assemblyLineModel::monitoringResponse)));
        });
  }

  private static ControlUnitService controlUnitSvc(RequesterFactory requesterFactory) {
    return requesterFactory.create(ControlUnitService.class);
  }

  /*periodically ask peer about available power*/
  private void pollControlUnitPower() {
    Flux.interval(Duration.ofSeconds(7))
        .flatMap(__ -> controlUnitSvc.power())
        .subscribe(renderer::powerChanged, renderer::powerError);
  }
}
