package com.github.mostroverkhov.r2;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static com.github.mostroverkhov.r2.ProtoDefs.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProtoCodecTest {

    private ProtobufCodec codec;

    @Before
    public void setUp() throws Exception {
        codec = new ProtobufCodec();
    }

    @Test
    public void codec() throws Exception {
        Person person = stubPerson();
        ByteBuffer buffer = codec.encode(person);
        Person decodedPerson = codec.decode(buffer, Person.class);
        assertEquals(person, decodedPerson);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeNonProto() {
        codec.encode(new Foo("bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeNonProto() {
        ByteBuffer encoded = codec.encode(stubPerson());
        codec.decode(encoded, Foo.class);
    }

    private Person stubPerson() {
        Person.PhoneNumber phone = Person.PhoneNumber
                .newBuilder()
                .setNumber("123")
                .setType(Person.PhoneType.MOBILE)
                .build();

        Person person = Person.newBuilder()
                .addPhones(phone)
                .setId(42)
                .setName("name")
                .setEmail("email")
                .build();

        return person;
    }

    private class Foo {
        private final String bar;

        public Foo(String bar) {
            this.bar = bar;
        }

        public String getBar() {
            return bar;
        }
    }
}
