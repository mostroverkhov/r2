package com.github.mostroverkhov.r2.codec.jackson;

import com.github.mostroverkhov.r2.core.DataCodec;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JacksonBinaryCodecTest extends BaseJacksonDataCodecTest {

  @Parameter(0)
  public DataCodec dataCodec;

  @Parameter(1)
  public String prefix;

  @Test
  public void codec() {
    verifyCodec(dataCodec);
  }

  @Test
  public void prefix() {
    verifyPrefix(dataCodec, prefix);
  }

  @Parameters
  public static Collection<Object[]> input() {
    return Arrays.asList(
        new Object[][] {
          {new JacksonCborDataCodec(), "cbor"},
          {new JacksonSmileDataCodec(), "smile"}
        });
  }
}
