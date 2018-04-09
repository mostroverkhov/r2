package com.github.mostroverkhov.r2.codec.jackson;

import com.github.mostroverkhov.r2.core.DataCodec;
import org.junit.Before;
import org.junit.Test;

public class JacksonJsonDataCodecTest extends BaseJacksonDataCodecTest {

  private DataCodec dataCodec;

  @Before
  public void setUp() throws Exception {
    dataCodec = new JacksonJsonDataCodec();
  }

  @Test
  public void codec() {
    verifyCodec(dataCodec);
  }

  @Test
  public void prefix() {
    verifyPrefix(dataCodec, "json");
  }
}
