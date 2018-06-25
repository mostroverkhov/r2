package com.github.mostroverkhov.r2.core

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class Metadata private constructor(private val svcMethod: ByteBuffer?,
                                   private val keyValues: Map<String, () -> ByteBuffer>) {

    fun data(key: String): ByteArray? = keyValues[key]?.invoke()?.toArray()

    fun keys() = keyValues.keys

    fun auth() = data(Keys.AUTH())

    fun hasSvcMethod(): Boolean = svcMethod != null

    fun svcMethod(): ByteArray? = svcMethod?.toArray()

    fun asByteBuffer(): AsByteBuffer = AsByteBuffer()

    private fun ByteBuffer.toArray(): ByteArray {
        val arr = ByteArray(remaining())
        mark()
        get(arr)
        reset()
        return arr
    }

    inner class AsByteBuffer {

        fun keys() = this@Metadata.keys()

        fun data(key: String): (() -> ByteBuffer)? = keyValues[key]

        fun svcMethod(): ByteBuffer? = svcMethod

        fun hasSvcMethod() = this@Metadata.hasSvcMethod()

        fun keyValues() = keyValues
    }

    open class Builder {
        internal val keyValues = ConcurrentHashMap<String, () -> ByteBuffer>()

        fun data(key: String, value: ByteBuffer): Builder {
            return data(key, { value })
        }

        fun data(key: String, value: () -> ByteBuffer): Builder {
            keyValues[key] = value
            return this
        }

        fun data(key: String, value: ByteArray): Builder {
            return data(key, { ByteBuffer.wrap(value) })
        }

        fun auth(value: ByteArray): Builder = data(Keys.AUTH(), value)

        open fun build(): Metadata = Metadata(null, keyValues)
    }

    internal class RequestBuilder : Builder() {
        private lateinit var svcMethod: ByteBuffer

        fun metadata(metadata: Metadata?): RequestBuilder {
            metadata?.let {
                val md = it.asByteBuffer()
                md.keys().forEach { key ->
                    data(key, md.data(key)!!)
                }
            }
            return this
        }

        fun svcMethod(svcMethod: ByteBuffer): RequestBuilder {
            this.svcMethod = svcMethod
            return this
        }

        override fun build(): Metadata = Metadata(svcMethod, keyValues)
    }


    enum class Keys(val value: String) {
        AUTH("auth");

        operator fun invoke(): String = value
    }
}