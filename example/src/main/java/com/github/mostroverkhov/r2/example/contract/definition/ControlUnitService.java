package com.github.mostroverkhov.r2.example.contract.definition;

import com.github.mostroverkhov.r2.core.contract.RequestResponse;
import com.github.mostroverkhov.r2.core.contract.Service;
import com.github.mostroverkhov.r2.example.contract.model.ControlUnitResponse;
import org.reactivestreams.Publisher;

@Service("controlUnit")
public interface ControlUnitService {

  @RequestResponse("power")
  Publisher<ControlUnitResponse> power();
}
