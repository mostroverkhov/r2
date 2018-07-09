package com.github.mostroverkhov.r2.core.contract.gen;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContractCompilerTest {
  @Test
  public void compileReactor() {
    JavaFileObject source = sourceContract();
    JavaFileObject expected = JavaFileObjects.forSourceString(
        "com.github.mostroverkhov.r2.core.contract.gen.reactor.TestContract", ""
            + "package com.github.mostroverkhov.r2.core.contract.gen.reactor;\n"
            + "\n"
            + "import com.github.mostroverkhov.r2.core.contract.Close;\n"
            + "import com.github.mostroverkhov.r2.core.contract.FireAndForget;\n"
            + "import com.github.mostroverkhov.r2.core.contract.OnClose;\n\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestChannel;\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestResponse;\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestStream;\n"
            + "import com.github.mostroverkhov.r2.core.contract.Service;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Bar;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Foo;\n"
            + "import java.lang.Void;\n"
            + "import reactor.core.publisher.Flux;\n"
            + "import reactor.core.publisher.Mono;\n"
            + "\n"
            + "@Service(\"test\")\n"
            + "public interface TestContract {\n"
            + "\n"
            + "@RequestChannel(\"channel\")\n"
            + "Flux<Foo> channel(Flux<Bar> bar);\n"
            + "\n"
            + "@RequestStream(\"stream\")\n"
            + "Flux<Bar> stream(Foo foo);\n"
            + "\n"
            + "@RequestResponse(\"response\")\n"
            + "Mono<Bar> response(Foo foo);\n"
            + "\n"
            + "@FireAndForget(\"fnf\")\n"
            + "Mono<Void> fnf(Foo foo);\n"
            + "\n"
            + "@Close()\n"
            + "Mono<Void> close();\n"
            + "\n"
            + "@OnClose()\n"
            + "Mono<Void> onClose();\n"
            + "}\n"
    );

    assertAbout(javaSource()).that(source)
        .withCompilerOptions("-Ar2.gen.platform=reactor")
        .withCompilerOptions("-Ar2.gen.package=com.github.mostroverkhov.r2.core.contract.gen")
        .withCompilerOptions("-Ar2.gen.enabled=true")
        .processedWith(new ContractsCompiler())
        .compilesWithoutError()
        .and()
        .generatesSources(expected);
  }

  @Test
  public void compileRxJava() {
    JavaFileObject source = sourceContract();
    JavaFileObject expected = JavaFileObjects.forSourceString(
        "com.github.mostroverkhov.r2.core.contract.gen.rxjava.TestContract", ""
            + "package com.github.mostroverkhov.r2.core.contract.gen.rxjava;\n"
            + "\n"
            + "import com.github.mostroverkhov.r2.core.contract.Close;\n"
            + "import com.github.mostroverkhov.r2.core.contract.FireAndForget;\n"
            + "import com.github.mostroverkhov.r2.core.contract.OnClose;\n\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestChannel;\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestResponse;\n"
            + "import com.github.mostroverkhov.r2.core.contract.RequestStream;\n"
            + "import com.github.mostroverkhov.r2.core.contract.Service;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Bar;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Foo;\n"
            + "import io.reactivex.Completable;\n"
            + "import io.reactivex.Flowable;\n"
            + "import io.reactivex.Single;\n"
            + "\n"
            + "@Service(\"test\")\n"
            + "public interface TestContract {\n"
            + "\n"
            + "@RequestChannel(\"channel\")\n"
            + "Flowable<Foo> channel(Flowable<Bar> bar);\n"
            + "\n"
            + "@RequestStream(\"stream\")\n"
            + "Flowable<Bar> stream(Foo foo);\n"
            + "\n"
            + "@RequestResponse(\"response\")\n"
            + "Single<Bar> response(Foo foo);\n"
            + "\n"
            + "@FireAndForget(\"fnf\")\n"
            + "Completable fnf(Foo foo);\n"
            + "\n"
            + "@Close()\n"
            + "Completable close();\n"
            + "\n"
            + "@OnClose()\n"
            + "Completable onClose();\n"
            + "}\n"
    );

    assertAbout(javaSource()).that(source)
        .withCompilerOptions("-Ar2.gen.platform=rxjava")
        .withCompilerOptions("-Ar2.gen.package=com.github.mostroverkhov.r2.core.contract.gen")
        .withCompilerOptions("-Ar2.gen.enabled=true")
        .processedWith(new ContractsCompiler())
        .compilesWithoutError()
        .and()
        .generatesSources(expected);
  }

  @Test
  public void missingPlatform() {
    assertAbout(javaSource()).that(sourceContract())
        .withCompilerOptions("-Ar2.gen.package=com.github.mostroverkhov.r2.core.contract.gen")
        .withCompilerOptions("-Ar2.gen.enabled=true")
        .processedWith(new ContractsCompiler())
        .failsToCompile();
  }

  @Test
  public void missingPackage() {
    assertAbout(javaSource()).that(sourceContract())
        .withCompilerOptions("-Ar2.gen.platform=rxjava")
        .withCompilerOptions("-Ar2.gen.enabled=true")
        .processedWith(new ContractsCompiler())
        .failsToCompile();
  }


  private JavaFileObject sourceContract() {
    return JavaFileObjects.forSourceString(
        "com.github.mostroverkhov.r2.core.contract.gen.TestContract", ""
            + "package com.github.mostroverkhov.r2.core.contract.gen;\n"
            + "\n"
            + "import com.github.mostroverkhov.r2.core.contract.*;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Bar;\n"
            + "import com.github.mostroverkhov.r2.core.contract.gen.model.Foo;\n"
            + "import org.reactivestreams.Publisher;\n"
            + "\n"
            + "@Service(\"test\")\n"
            + "public interface TestContract {\n"
            + "\n"
            + "@RequestChannel(\"channel\")\n"
            + "Publisher<Foo> channel(Publisher<Bar> bar);\n"
            + "\n"
            + "@RequestStream(\"stream\")\n"
            + "Publisher<Bar> stream(Foo foo);\n"
            + "\n"
            + "@RequestResponse(\"response\")\n"
            + "Publisher<Bar> response(Foo foo);\n"
            + "\n"
            + "@FireAndForget(\"fnf\")\n"
            + "Publisher<Bar> fnf(Foo foo);\n"
            + "\n"
            + "@Close()\n"
            + "Publisher<Void> close();\n"
            + "\n"
            + "@OnClose()\n"
            + "Publisher<Void> onClose();\n"
            + "}\n"
    );
  }
}
