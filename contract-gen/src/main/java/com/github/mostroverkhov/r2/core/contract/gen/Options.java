package com.github.mostroverkhov.r2.core.contract.gen;

import com.github.mostroverkhov.r2.core.contract.gen.dest.WriteDest;

import java.util.function.Function;

public class Options {
  private final String platform;
  private final String pkg;
  private Function<String, WriteDest> dest;

  public Options(String platform,
                 String pkg,
                 Function<String, WriteDest> dest) {
    this.platform = platform;
    this.pkg = pkg;
    this.dest = dest;
  }

  public String getPlatform() {
    return platform;
  }

  public String getPkg() {
    return pkg;
  }

  public Function<String, WriteDest> getDest() {
    return dest;
  }
}
