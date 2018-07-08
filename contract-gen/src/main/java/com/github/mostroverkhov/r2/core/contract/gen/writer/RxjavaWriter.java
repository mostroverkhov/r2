package com.github.mostroverkhov.r2.core.contract.gen.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class RxjavaWriter extends BaseContractWriter {
  public RxjavaWriter() {
    super(new RxjavaAdapter());
  }

  private static class RxjavaAdapter implements Adapter {

    @Override
    public ReturnMapper returnZero() {
      return t -> TypeName.get(Completable.class);
    }

    @Override
    public ReturnMapper returnOne() {
      return t -> ParameterizedTypeName.get(
          ClassName.get(Single.class),
          TypeName.get(t));
    }

    @Override
    public ReturnMapper returnMany() {
      return t -> ParameterizedTypeName.get(
          ClassName.get(Flowable.class),
          TypeName.get(t));
    }

    @Override
    public Class<?> argPublisher() {
      return Flowable.class;
    }

    @Override
    public String updatePackage(String pkg) {
      return pkg + ".rxjava";
    }
  }
}
