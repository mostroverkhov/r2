package com.github.mostroverkhov.r2.core.contract.gen.writer;

import com.github.mostroverkhov.r2.core.contract.gen.model.WriteContract;
import com.github.mostroverkhov.r2.core.contract.gen.dest.WriteDest;
import com.github.mostroverkhov.r2.core.contract.gen.model.ReadContract;
import com.squareup.javapoet.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BaseContractWriter implements ContractWriter {
  private Adapter adapter;

  BaseContractWriter(Adapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public void write(WriteContract contractWithWriteDest) {
    ReadContract readContract = contractWithWriteDest.getReadContract();
    List<ReadContract.Method> methods = readContract.getMethods();
    List<MethodSpec> methodSpecs = new ArrayList<>();

    for (ReadContract.Method method : methods) {
      MethodSpec.Builder mb = MethodSpec.methodBuilder(method.getName());
      mb.returns(adaptReturnTypes(method));
      for (VariableElement arg : method.getArgs()) {
        ParameterSpec parameterSpec = adaptParam(arg);
        mb.addParameter(parameterSpec);
      }
      for (AnnotationMirror annotation : method.getAnnotations()) {
        AnnotationSpec annotationSpec = AnnotationSpec.get(annotation);
        mb.addAnnotation(annotationSpec);
      }
      MethodSpec methodSpec = mb
          .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
          .build();
      methodSpecs.add(methodSpec);
    }
    ReadContract.Type type = readContract.getType();
    List<AnnotationMirror> annotations = type.getAnnotations();

    ArrayList<AnnotationSpec> annoSpecs = new ArrayList<>();
    for (AnnotationMirror annotation : annotations) {
      AnnotationSpec annotationSpec = AnnotationSpec.get(annotation);
      annoSpecs.add(annotationSpec);
    }
    TypeSpec typeSpec = TypeSpec.interfaceBuilder(type.getName())
        .addModifiers(Modifier.PUBLIC)
        .addMethods(methodSpecs)
        .addAnnotations(annoSpecs)
        .build();

    contractWithWriteDest.updatePackage(adapter::updatePackage);

    JavaFile javaFile = JavaFile
        .builder(contractWithWriteDest.getPackage(), typeSpec)
        .build();
    try {
      WriteDest writeDest = contractWithWriteDest.createWriteDest();
      javaFile.writeTo(writeDest);
      writeDest.close();
    } catch (IOException e) {
      throw new RuntimeException("Error writing contract", e);
    }
  }

  private static ParameterSpec adaptArgumentTypes(VariableElement arg,
                                                    Class<?> targetType) {
    TypeMirror typeMirror = arg.asType();
    String varName = arg.getSimpleName().toString();
    if (typeMirror.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      Element element = declaredType.asElement();
      TypeElement typeElement = (TypeElement) element;
      if (isPublisher(typeElement.getQualifiedName())) {
        TypeMirror typeParam = declaredType.getTypeArguments().get(0);
        TypeName argClass = TypeName.get(typeParam);
        ClassName className = ClassName.get(targetType);
        ParameterizedTypeName typeName = ParameterizedTypeName
            .get(className, argClass);
        return ParameterSpec
            .builder(typeName, varName)
            .build();
      } else {
        TypeName typeName = TypeName.get(typeElement.asType());
        return ParameterSpec
            .builder(typeName, varName)
            .build();
      }
    } else {
      throw nonReferenceVarError(arg);
    }
  }

  private ParameterSpec adaptParam(VariableElement arg) {
    return adaptArgumentTypes(arg, adapter.argPublisher());
  }

  private TypeName adaptReturnTypes(ReadContract.Method method) {
    ReadContract.Method.Interaction interaction = method.getInteraction();
    TypeMirror returnTypeArg = method.getReturnTypeArg();
    switch (interaction) {
      case FNF:
      case CLOSE:
      case ONCLOSE:
        return adapter.returnZero().apply(returnTypeArg);
      case REQUEST:
        return adapter.returnOne().apply(returnTypeArg);
      case STREAM:
      case CHANNEL:
        return adapter.returnMany().apply(returnTypeArg);
      default:
        throw new AssertionError("Unknown interaction: " + interaction);
    }
  }

  protected interface Adapter {

    ReturnMapper returnZero();

    ReturnMapper returnOne();

    ReturnMapper returnMany();

    Class<?> argPublisher();

    String updatePackage(String pkg);

    interface ReturnMapper extends Function<TypeMirror, TypeName> {
    }
  }

  private static boolean isPublisher(Name qualifiedName) {
    return qualifiedName.contentEquals("org.reactivestreams.Publisher");
  }

  private static IllegalArgumentException nonReferenceVarError(
      VariableElement arg) {
    return new IllegalArgumentException(
        "Variable " + arg.getSimpleName() + " must be of reference type");
  }
}
