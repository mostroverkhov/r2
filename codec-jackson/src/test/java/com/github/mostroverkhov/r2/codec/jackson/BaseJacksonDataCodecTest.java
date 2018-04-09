package com.github.mostroverkhov.r2.codec.jackson;

import static org.junit.Assert.assertEquals;

import com.github.mostroverkhov.r2.core.DataCodec;
import java.nio.ByteBuffer;
import java.util.Objects;
import org.junit.Assert;

abstract class BaseJacksonDataCodecTest {

  static void verifyCodec(DataCodec dataCodec) {
    Stub stub = new Stub("foo", "bar");
    ByteBuffer encoded = dataCodec.encode(stub);
    Stub decoded = dataCodec.decode(encoded, Stub.class);
    assertEquals(stub, decoded);
  }

  static void verifyPrefix(DataCodec dataCodec, String prefix) {
    Assert.assertEquals(prefix, dataCodec.getPrefix());
  }

  static class Stub {
    String foo;
    String bar;

    public Stub(String foo, String bar) {
      this.foo = foo;
      this.bar = bar;
    }

    public Stub() {}

    public String getFoo() {
      return foo;
    }

    public void setFoo(String foo) {
      this.foo = foo;
    }

    public String getBar() {
      return bar;
    }

    public void setBar(String bar) {
      this.bar = bar;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      JacksonJsonDataCodecTest.Stub stub = (JacksonJsonDataCodecTest.Stub) o;
      return Objects.equals(foo, stub.foo) && Objects.equals(bar, stub.bar);
    }

    @Override
    public int hashCode() {

      return Objects.hash(foo, bar);
    }
  }
}
