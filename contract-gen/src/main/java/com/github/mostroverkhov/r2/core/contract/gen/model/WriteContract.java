package com.github.mostroverkhov.r2.core.contract.gen.model;

import com.github.mostroverkhov.r2.core.contract.gen.dest.WriteDest;

import java.util.function.Function;

public class WriteContract {
  private final Function<String, WriteDest> target;
  private String pkg;
  private final ReadContract readContract;

  public WriteContract(Function<String, WriteDest> target,
                       String pkg,
                       ReadContract readContract) {
    this.target = target;
    this.pkg = pkg;
    this.readContract = readContract;
  }

  public WriteDest createWriteDest() {
    return target.apply(filePath(getReadContract()));
  }

  public String getPackage() {
    return pkg;
  }

  public ReadContract getReadContract() {
    return readContract;
  }

  public void updatePackage(Function<String, String> function) {
    pkg = function.apply(pkg);
  }

  private String filePath(ReadContract readContract) {
    return getPackage() + "." + readContract.getType().getName();
  }
}
