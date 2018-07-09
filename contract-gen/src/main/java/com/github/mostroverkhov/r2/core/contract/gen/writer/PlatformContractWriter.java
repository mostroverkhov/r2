package com.github.mostroverkhov.r2.core.contract.gen.writer;

import com.github.mostroverkhov.r2.core.contract.gen.Options;
import com.github.mostroverkhov.r2.core.contract.gen.model.ReadContract;
import com.github.mostroverkhov.r2.core.contract.gen.model.WriteContract;

import java.util.HashMap;
import java.util.Map;

public class PlatformContractWriter implements ContractWriter {
  private static final Map<String, ContractWriter> writers = new HashMap<>();

  static {
    writers.put("rxjava", new RxjavaWriter());
    writers.put("reactor", new ReactorWriter());
  }

  private final ContractWriter contractWriter;
  private Options options;

  public PlatformContractWriter(Options options) {
    this.options = options;
    String platform = options.getPlatform();
    ContractWriter contractWriter = writers.get(platform);
    if (contractWriter == null) {
      throw new IllegalArgumentException("Unsupported platform: " + platform);
    }
    this.contractWriter = contractWriter;
  }

  @Override
  public void write(WriteContract writeContract) {
    contractWriter.write(writeContract);
  }

  public void write(ReadContract readContract) {
    WriteContract writeContract = new WriteContract(
        options.getDest(),
        options.getPkg(),
        readContract);
    contractWriter.write(writeContract);
  }
}
