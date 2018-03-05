package com.github.mostroverkhov.r2.codec.jackson

import com.fasterxml.jackson.databind.ObjectMapper

class JacksonJsonDataCodec(objectMapper: ObjectMapper) : BaseJacksonDataCodec(objectMapper) {

    constructor() : this(ObjectMapper())

    override val prefix = "json"
}
