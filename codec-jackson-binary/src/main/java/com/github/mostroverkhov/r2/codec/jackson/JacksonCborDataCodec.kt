package com.github.mostroverkhov.r2.codec.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory

class JacksonCborDataCodec(cborFactory: CBORFactory,
                           mapperConfigurer: (ObjectMapper) -> ObjectMapper)
    : JacksonBinaryDataCodec<CBORFactory>(cborFactory, mapperConfigurer) {

    constructor() : this(CBORFactory(), { it })

    override val prefix = "cbor"
}