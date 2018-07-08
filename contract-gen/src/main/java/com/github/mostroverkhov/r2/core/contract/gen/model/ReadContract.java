package com.github.mostroverkhov.r2.core.contract.gen.model;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class ReadContract {
  private final Type type;
  private final List<Method> methods;

  public ReadContract(Type type,
                      List<Method> methods) {
    this.methods = methods;
    this.type = type;
  }

  public List<Method> getMethods() {
    return methods;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "Contract{" +
        "methods=" + methods +
        ", type=" + type +
        '}';
  }

  public static class Method {
    private final Interaction interaction;
    private final String name;
    private final TypeMirror returnTypeArg;
    private final List<VariableElement> args;
    private final List<AnnotationMirror> annotations;

    public Method(Interaction interaction,
                  String name,
                  TypeMirror returnTypeArg,
                  List<VariableElement> args,
                  List<AnnotationMirror> annotations) {
      this.interaction = interaction;
      this.name = name;
      this.returnTypeArg = returnTypeArg;
      this.args = args;
      this.annotations = annotations;
    }

    public Interaction getInteraction() {
      return interaction;
    }

    public String getName() {
      return name;
    }

    public TypeMirror getReturnTypeArg() {
      return returnTypeArg;
    }

    public List<VariableElement> getArgs() {
      return args;
    }

    public List<AnnotationMirror> getAnnotations() {
      return annotations;
    }

    @Override
    public String toString() {
      return "Method{" +
          "interaction=" + interaction +
          ", name='" + name + '\'' +
          ", returnTypeArg=" + returnTypeArg +
          ", args=" + args +
          ", annotations=" + annotations +
          '}';
    }

    public enum Interaction {
      FNF("com.github.mostroverkhov.r2.core.contract.FireAndForget"),
      REQUEST("com.github.mostroverkhov.r2.core.contract.RequestResponse"),
      STREAM("com.github.mostroverkhov.r2.core.contract.RequestStream"),
      CHANNEL("com.github.mostroverkhov.r2.core.contract.RequestChannel"),
      CLOSE("com.github.mostroverkhov.r2.core.contract.Close"),
      ONCLOSE("com.github.mostroverkhov.r2.core.contract.OnClose");

      private final String type;

      Interaction(String type) {
        this.type = type;
      }

      public static Optional<Interaction> get(String name) {
        for (Interaction value : Interaction.values()) {
          if (value.type.equals(name)) {
            return Optional.of(value);
          }
        }
        return Optional.empty();
      }
    }
  }

  public static class Type {
    private final String name;
    private final List<AnnotationMirror> annotations;

    public Type(String name, List<AnnotationMirror> annotations) {
      this.name = name;
      this.annotations = annotations;
    }

    public String getName() {
      return name;
    }

    public List<AnnotationMirror> getAnnotations() {
      return annotations;
    }

    @Override
    public String toString() {
      return "Type{" +
          "name='" + name + '\'' +
          ", annotations=" + annotations +
          '}';
    }
  }
}
