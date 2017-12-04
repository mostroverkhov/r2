package com.github.mostroverkhov.r2.core.responder

import com.github.mostroverkhov.r2.core.DataCodec
import java.util.concurrent.ConcurrentHashMap

class Codecs : CodecReader {

    private val codecs = ConcurrentHashMap<String, DataCodec>()

    override fun get(codec: String): DataCodec? = codecs[codec]

    fun add(dataCodec: DataCodec): Codecs {
        codecs[dataCodec.prefix] = dataCodec
        return this
    }
}

interface CodecReader {

    operator fun get(codec: String): DataCodec?
}