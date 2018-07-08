package com.github.mostroverkhov.r2.core.contract.gen.reader;

import com.github.mostroverkhov.r2.core.contract.gen.model.ReadContract;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ContractsReader {
  private final ContractReader contractReader = new ContractReader();

  public Set<ReadContract> read(Set<Element> contracts) {
    return contracts.stream()
        .filter(this::isInterface)
        .map(this::asTypeElement)
        .map(contractReader::read)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());

  }

  private boolean isInterface(Element contract) {
    return contract.getKind() == ElementKind.INTERFACE;
  }

  private TypeElement asTypeElement(Element element) {
    return (TypeElement) element;
  }
}
