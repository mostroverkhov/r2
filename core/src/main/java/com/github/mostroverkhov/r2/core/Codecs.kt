package com.github.mostroverkhov.r2.core

import java.util.concurrent.ConcurrentHashMap

class Codecs : CodecReader {
    private val codecs = ConcurrentHashMap<String, DataCodec>()
    private var primary: DataCodec? = null

    override fun get(codec: String): DataCodec? = codecs[codec]

    fun add(dataCodec: DataCodec): Codecs {
        if (primary == null) {
            primary = dataCodec
        }
        codecs[dataCodec.prefix] = dataCodec
        return this
    }

    override fun primary(): DataCodec = primary
            ?: throw IllegalArgumentException("No primary codec was set")

    fun primary(codec: String): Codecs {
        val dataCodec = codecs[codec]
        primary = dataCodec ?: throw IllegalArgumentException("Unknown codec as " +
                "primary candidate: $dataCodec")

        return this
    }
}

interface CodecReader {

    operator fun get(codec: String): DataCodec?

    fun primary(): DataCodec
}