package com.github.mostroverkhov.r2.codec.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import com.github.mostroverkhov.r2.core.DataCodec
import java.nio.ByteBuffer

abstract class BaseJacksonDataCodec(private val objectMapper: ObjectMapper) : DataCodec {

    override fun <T> encode(obj: T): ByteBuffer = ByteBuffer
            .wrap(objectMapper.writeValueAsBytes(obj))

    override fun <T> decode(buf: ByteBuffer, clazz: Class<T>)
            = objectMapper.readValue<T>(ByteBufferBackedInputStream(buf), clazz)
}
