package com.github.mostroverkhov.r2.example.contract.definition;

import com.github.mostroverkhov.r2.core.contract.RequestChannel;
import com.github.mostroverkhov.r2.core.contract.Service;
import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesRequest;
import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesResponse;
import org.reactivestreams.Publisher;

 @Service("assemblyLines")
public interface AssemblyLinesService {

  @RequestChannel("control")
  Publisher<AssemblyLinesResponse> control(Publisher<AssemblyLinesRequest> requests);
}
