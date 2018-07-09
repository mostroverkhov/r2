package com.github.mostroverkhov.r2.core.contract.gen.dest;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.io.Writer;

public class FileWriteDest implements WriteDest {
  private final Writer source;

  public FileWriteDest(Filer filer, String path) {
    this.source = createWriter(filer, path);
  }

  @Override
  public void close() throws IOException {
    source.close();
  }

  @Override
  public Appendable append(CharSequence csq) throws IOException {
    return source.append(csq);
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    return source.append(csq, start, end);
  }

  @Override
  public Appendable append(char c) throws IOException {
    return source.append(c);
  }

  private Writer createWriter(Filer filer, String path) {
    try {
      return filer.createSourceFile(path).openWriter();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
