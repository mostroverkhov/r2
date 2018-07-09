package com.github.mostroverkhov.r2.core.contract.gen;

import com.github.mostroverkhov.r2.core.contract.Service;
import com.github.mostroverkhov.r2.core.contract.gen.dest.FileWriteDest;
import com.github.mostroverkhov.r2.core.contract.gen.dest.WriteDest;
import com.github.mostroverkhov.r2.core.contract.gen.reader.ContractsReader;
import com.github.mostroverkhov.r2.core.contract.gen.writer.PlatformContractWriter;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.github.mostroverkhov.r2.core.contract.Service")
public class ContractsCompiler extends AbstractProcessor {

  private ContractsReader contractsReader;
  private PlatformContractWriter contractWriter;
  private boolean enabled;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    messager = processingEnv.getMessager();
    Elements elementUtils = processingEnv.getElementUtils();
    Map<String, String> mapOptions = processingEnv.getOptions();
    Filer filer = processingEnv.getFiler();

    if (isSet("r2.gen.enabled", mapOptions)) {
      Optional<Options> options = options(mapOptions, fileDest(filer));
      enabled = options.isPresent();
      if (enabled) {
        contractsReader = new ContractsReader(elementUtils);
        contractWriter = new PlatformContractWriter(options.get());
      } else {
        printMissingOptionsError();
      }
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations,
                         RoundEnvironment roundEnv) {
    if (enabled) {
      contractsReader
          .read(contracts(roundEnv))
          .forEach(contract -> contractWriter.write(contract));
    }
    return false;
  }

  private void printMissingOptionsError() {
    messager.printMessage(Diagnostic.Kind.ERROR,
        "r2.gen.platform and r2.gen.package must be present");
  }

  @SuppressWarnings("unchecked")
  private Set<Element> contracts(RoundEnvironment roundEnv) {
    return (Set<Element>) roundEnv.getElementsAnnotatedWith(Service.class);
  }

  private Optional<Options> options(Map<String, String> processorOpts,
                                    Function<String, WriteDest> writeDest) {
    Optional<String> platform = option("r2.gen.platform", processorOpts);
    Optional<String> pkg = option("r2.gen.package", processorOpts);

    return platform
        .flatMap(pl -> pkg
            .map(pk -> new Options(pl, pk, writeDest)));
  }

  private Function<String, WriteDest> fileDest(Filer filer) {
    return path -> new FileWriteDest(filer, path);
  }

  private boolean isSet(String key,
                        Map<String, String> processorOpts) {
    return Optional.ofNullable(
        processorOpts.get(key))
        .map(Boolean::parseBoolean)
        .orElse(false);
  }

  private Optional<String> option(String key, Map<String, String> map) {
    return Optional.ofNullable(map.get(key));
  }
}
