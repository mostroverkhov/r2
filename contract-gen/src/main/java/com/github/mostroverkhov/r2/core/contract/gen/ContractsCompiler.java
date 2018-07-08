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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.github.mostroverkhov.r2.core.contract.Service")
public class ContractsCompiler extends AbstractProcessor {

  private ContractsReader contractsReader;
  private PlatformContractWriter contractWriter;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    Map<String, String> mapOptions = processingEnv.getOptions();
    Filer filer = processingEnv.getFiler();

    Options options = options(mapOptions, fileDest(filer));
    contractsReader = new ContractsReader();
    contractWriter = new PlatformContractWriter(options);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations,
                         RoundEnvironment roundEnv) {
    contractsReader
        .read(contracts(roundEnv))
        .forEach(contract -> contractWriter.write(contract));

    return false;
  }

  @SuppressWarnings("unchecked")
  private Set<Element> contracts(RoundEnvironment roundEnv) {
    return (Set<Element>) roundEnv.getElementsAnnotatedWith(Service.class);
  }

  private Options options(Map<String, String> processorOpts,
                          Function<String, WriteDest> writeDest) {
    String platform = getOrThrow("platform", processorOpts);
    String pkg = getOrThrow("package", processorOpts);
    return new Options(platform, pkg, writeDest);
  }

  private Function<String, WriteDest> fileDest(Filer filer) {
    return path -> new FileWriteDest(filer, path);
  }

  private String getOrThrow(String key, Map<String, String> map) {
    String val = map.get(key);
    if (val == null) {
      throw new IllegalArgumentException("missing option: " + key);
    }
    return val;
  }
}
