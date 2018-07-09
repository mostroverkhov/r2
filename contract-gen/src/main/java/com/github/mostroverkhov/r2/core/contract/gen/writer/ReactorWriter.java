package com.github.mostroverkhov.r2.core.contract.gen.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorWriter extends BaseContractWriter {

  public ReactorWriter() {
    super(new ReactorAdapter());
  }

  private static class ReactorAdapter implements Adapter {

    @Override
    public ReturnMapper returnZero() {
      return m -> ParameterizedTypeName
          .get(ClassName.get(Mono.class), TypeName.get(Void.class));
    }

    @Override
    public ReturnMapper returnOne() {
      return m -> ParameterizedTypeName
          .get(ClassName.get(Mono.class), TypeName.get(m));
    }

    @Override
    public ReturnMapper returnMany() {
      return m -> ParameterizedTypeName
          .get(ClassName.get(Flux.class), TypeName.get(m));
    }

    @Override
    public Class<?> argPublisher() {
      return Flux.class;
    }

    @Override
    public String updatePackage(String pkg) {
      return pkg + ".reactor";
    }
  }
}
