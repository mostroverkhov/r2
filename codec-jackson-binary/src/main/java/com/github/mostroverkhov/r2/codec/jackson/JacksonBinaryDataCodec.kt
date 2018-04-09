package com.github.mostroverkhov.r2.codec.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper

abstract class JacksonBinaryDataCodec<T : JsonFactory>(binaryJsonFactory: T,
                                                       mapperConfigurer: (ObjectMapper) -> ObjectMapper)
    : BaseJacksonDataCodec(mapperConfigurer(ObjectMapper(binaryJsonFactory)))