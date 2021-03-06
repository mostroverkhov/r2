package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.*

inline fun <reified T> RequesterFactory.create(): T = create(T::class.java)

operator fun Codecs.plus(dataCodec: DataCodec): Codecs = add(dataCodec)

operator fun Services.plus(service: Any): Services = add(service)

operator fun Services.plus(service: Pair<String, Any>): Services = add(
        service.first,
        service.second)
