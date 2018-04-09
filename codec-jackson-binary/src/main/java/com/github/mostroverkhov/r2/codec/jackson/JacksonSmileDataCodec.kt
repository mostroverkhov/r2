package com.github.mostroverkhov.r2.codec.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.SmileFactory

class JacksonSmileDataCodec(smileFactory: SmileFactory,
                            mapperConfigurer: (ObjectMapper) -> ObjectMapper)
    : JacksonBinaryDataCodec<SmileFactory>(smileFactory, mapperConfigurer) {

    constructor() : this(SmileFactory(), { it })

    override val prefix = "smile"
}