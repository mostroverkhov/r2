package com.github.mostroverkhov.r2.core.internal;

import com.github.mostroverkhov.r2.core.Metadata;
import kotlin.jvm.functions.Function0;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public class MetadataCodec {
    private static final int KEY_MAX_LENGTH = Byte.MAX_VALUE - Byte.MIN_VALUE;
    private static final int VALUE_MAX_LENGTH = Short.MAX_VALUE - Short.MIN_VALUE;
    private static final int KEY_SIZE = 1;
    private static final int VALUE_SIZE = 2;

    public ByteBuffer encode(Metadata metadata) {
        int metadataSize = calcSize(metadata);
        ByteBuffer bb = ByteBuffer.allocate(metadataSize);

        Metadata.AsByteBuffer asByteBuffer = metadata.asByteBuffer();
        encodeRoute(bb, asByteBuffer);

        Map<String, Function0<ByteBuffer>> keyValues = asByteBuffer.keyValues();
        for (Map.Entry<String, Function0<ByteBuffer>> keyValue : keyValues.entrySet()) {
            String key = keyValue.getKey();
            ByteBuffer value = keyValue.getValue().invoke();
            encodeKey(bb, key);
            encodeValue(bb, value);
        }
        bb.flip();
        return bb;
    }

    public Metadata decodeForRequest(ByteBuffer metadata) {
        return decode(metadata, true);
    }

    public Metadata decodeForConnection(ByteBuffer metadata) {
        return decode(metadata, false);
    }

    public Metadata decode(ByteBuffer metadata, boolean hasRoute) {
        Metadata.Builder builder = hasRoute
                ? new Metadata.RequestBuilder().route(decodeRoute(metadata))
                : new Metadata.Builder();

        while (metadata.remaining() > 0) {
            String key = decodeKey(metadata);
            ByteBuffer value = decodeValue(metadata);
            builder.data(key, value);
        }
        return builder.build();
    }

    private int calcSize(Metadata metadata) {
        int routeSize;
        if (metadata.hasRoute()) {
            ByteBuffer route = metadata.asByteBuffer().route();
            assertValueLength("route", route.remaining());
            routeSize = calcValueSize(route);
        } else {
            routeSize = 0;
        }

        int kvSize = 0;

        Set<Map.Entry<String, Function0<ByteBuffer>>> keyValues =
                metadata.asByteBuffer()
                        .keyValues()
                        .entrySet();
        for (Map.Entry<String, Function0<ByteBuffer>> keyValue : keyValues) {
            String key = keyValue.getKey();
            assertKeyLength(key);
            ByteBuffer value = keyValue.getValue().invoke();
            assertValueLength(key, value.remaining());

            kvSize += calcKeySize(key) + calcValueSize(value);
        }
        return routeSize + kvSize;
    }

    private int calcKeySize(String key) {
        return KEY_SIZE + key.length();
    }

    private int calcValueSize(ByteBuffer value) {
        return VALUE_SIZE + value.remaining();
    }

    private void encodeKey(ByteBuffer bb, String key) {
        bb.put((byte) key.length()).put(encodeKey(key));
    }

    private byte[] encodeKey(String key) {
        return key.getBytes(Charsets.US_ASCII);
    }

    private void encodeValue(ByteBuffer container, ByteBuffer value) {
        container.putShort((short) value.remaining()).put(value);
    }

    @NotNull
    private void encodeRoute(ByteBuffer result, Metadata.AsByteBuffer metadata) {
        if (metadata.hasRoute()) {
            ByteBuffer route = metadata.route();
            result.putShort((short) route.remaining()).put(route);
        }
    }

    @NotNull
    private ByteBuffer decodeValue(ByteBuffer metadata) {
        int valueLength = metadata.getShort() & 0xFFFF;
        ByteBuffer res = (ByteBuffer) metadata.slice().limit(valueLength);
        metadata.position(metadata.position() + valueLength);
        return res;
    }

    @NotNull
    private String decodeKey(ByteBuffer metadata) {
        int keyLength = metadata.get() & 0xFF;
        byte[] keyBytes = new byte[keyLength];
        metadata.get(keyBytes);
        return decodeKey(keyBytes);
    }

    @NotNull
    private String decodeKey(byte[] keyBytes) {
        return new String(keyBytes, Charsets.US_ASCII);
    }

    @NotNull
    private ByteBuffer decodeRoute(ByteBuffer metadata) {
        int length = metadata.getShort() & 0xFFFF;
        Buffer result = metadata.slice().limit(length);
        metadata.position(metadata.position() + length);
        return (ByteBuffer) result;
    }


    private static void assertValueLength(String key, int length) {
        if (length > VALUE_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("%s length %d exceeds max: %d",
                    key,
                    length,
                    VALUE_MAX_LENGTH));
        }
    }

    private static void assertKeyLength(String key) {
        int length = key.length();
        if (length > KEY_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("Key %s length %d exceeds max: %d",
                    key,
                    length,
                    KEY_MAX_LENGTH));
        }
    }
}
