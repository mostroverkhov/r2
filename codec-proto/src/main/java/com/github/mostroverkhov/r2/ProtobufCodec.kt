package com.github.mostroverkhov.r2

import com.github.mostroverkhov.r2.core.DataCodec
import com.google.protobuf.MessageLite
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class ProtobufCodec : DataCodec {
    private val cache = ConcurrentHashMap<Class<*>, Method>()
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
        val method = cache.getOrPut(clazz, { resolve(clazz) })
        val bytes = ByteArray(buf.remaining())
        buf.get(bytes)
        return method.invoke(null, bytes) as T
    }

    private fun <T> resolve(clazz: Class<T>): Method? {
        val method = try {
            clazz.getMethod("parseFrom", ByteArray::class.java)
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Class $clazz is not Protobuf generated class")
        }
        return method
    }
}