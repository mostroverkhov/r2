package com.github.mostroverkhov.r2

import com.github.mostroverkhov.r2.core.DataCodec
import com.google.protobuf.MessageLite
import java.nio.ByteBuffer

class ProtobufCodec : DataCodec {

    override val prefix = "proto"

    override fun <T> encode(obj: T): ByteBuffer {
        return if (obj is MessageLite) {
            ByteBuffer.wrap(obj.toByteArray())
        } else {
            throw IllegalArgumentException("$obj is Protobuf generated class")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(buf: ByteBuffer, clazz: Class<T>): T {
        val method = try {
            clazz.getMethod("parseFrom", ByteArray::class.java)
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Class $clazz is not Protobuf generated class")
        }
        val bytes = ByteArray(buf.remaining())
        buf.get(bytes)
        return method.invoke(null, bytes) as T
    }
}