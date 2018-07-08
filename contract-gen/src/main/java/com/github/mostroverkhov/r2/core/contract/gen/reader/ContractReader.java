package com.github.mostroverkhov.r2.core.contract.gen.reader;

import com.github.mostroverkhov.r2.core.contract.gen.model.ReadContract;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ContractReader {

  public Optional<ReadContract> read(TypeElement contract) {
    ReadContract.Type type = readType(contract);
    List<ReadContract.Method> methods = readMethods(contract);

    return methods.isEmpty()
        ? Optional.empty()
        : Optional.of(new ReadContract(type, methods));
  }

  private ReadContract.Type readType(TypeElement contract) {
    String name = contract.getSimpleName().toString();
    List<AnnotationMirror> annos = annotations(contract);
    return new ReadContract.Type(name, annos);
  }

  private Optional<ReadContract.Method> readMethod(ExecutableElement method) {
    List<AnnotationMirror> annotations = annotations(method);
    List<VariableElement> params = params(method);
    String name = method.getSimpleName().toString();
    TypeMirror returnType = method.getReturnType();
    DeclaredType declaredType = (DeclaredType) returnType;

    if (isPublisher(declaredType)) {
      TypeMirror typeParameterArg = declaredType.getTypeArguments().get(0);
      ReadContract.Method.Interaction interaction = interaction(annotations);

      return Optional.of(new ReadContract.Method(
          interaction,
          name,
          typeParameterArg,
          params,
          annotations
      ));
    } else {
      return Optional.empty();
    }

  }

  private boolean isPublisher(DeclaredType declaredType) {
    TypeElement typeElement = (TypeElement) declaredType.asElement();
    return typeElement.getQualifiedName()
        .contentEquals("org.reactivestreams.Publisher");
  }

  private ReadContract.Method.Interaction interaction(List<AnnotationMirror> annotations) {
    for (AnnotationMirror annotation : annotations) {
      DeclaredType annotationType = annotation.getAnnotationType();
      TypeElement typeElement = (TypeElement) annotationType.asElement();
      String name = typeElement.getQualifiedName().toString();
      Optional<ReadContract.Method.Interaction> interaction = ReadContract.Method.Interaction.get(name);
      if (interaction.isPresent()) {
        return interaction.get();
      }
    }
    throw new IllegalArgumentException(
        "No interaction annotations: " + annotations);
  }

  @SuppressWarnings("unchecked")
  private List<ReadContract.Method> readMethods(TypeElement contract) {
    List<ExecutableElement> els = (List<ExecutableElement>) contract.getEnclosedElements();
    return els.stream()
        .map(this::readMethod)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private List<VariableElement> params(ExecutableElement method) {
    return (List<VariableElement>) method.getParameters();
  }

  @SuppressWarnings("unchecked")
  private List<AnnotationMirror> annotations(AnnotatedConstruct annotated) {
    return (List<AnnotationMirror>)
        annotated.getAnnotationMirrors();
  }
}
