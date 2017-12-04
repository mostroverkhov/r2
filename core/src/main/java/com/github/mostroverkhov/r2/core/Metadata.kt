package com.github.mostroverkhov.r2.core

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class Metadata private constructor(private val route: ByteBuffer?,
                                   private val keyValues: Map<String, () -> ByteBuffer>) {

    fun data(key: String): ByteArray? = keyValues[key]?.invoke()?.toArray()

    fun hasRoute(): Boolean = route != null

    fun route(): ByteArray? = route?.toArray()

    fun keys() = keyValues.keys

    fun auth() = data(Keys.AUTH())

    fun asByteBuffer(): AsByteBuffer = AsByteBuffer()

    private fun ByteBuffer.toArray(): ByteArray {
        val arr = ByteArray(remaining())
        mark()
        get(arr)
        reset()
        return arr
    }

    inner class AsByteBuffer {
        fun data(key: String): (() -> ByteBuffer)? = keyValues[key]

        fun route(): ByteBuffer? = route

        fun hasRoute() = this@Metadata.hasRoute()

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
        private lateinit var route: ByteBuffer

        fun route(route: ByteBuffer): Builder {
            this.route = route
            return this
        }

        override fun build(): Metadata = Metadata(route, keyValues)
    }


    enum class Keys(val value: String) {
        AUTH("auth");

        operator fun invoke(): String = value
    }
}