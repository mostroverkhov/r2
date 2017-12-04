package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.requester.RequesterFactory
import com.github.mostroverkhov.r2.core.responder.Codecs
import com.github.mostroverkhov.r2.core.responder.Services

inline fun <reified T> RequesterFactory.create(): T = create(T::class.java)

operator fun Codecs.plus(dataCodec: DataCodec): Codecs = add(dataCodec)

operator fun Services.plus(service: Any): Services = add(service)

operator fun Services.plus(service: Pair<String, Any>): Services = add(
        service.first,
        service.second)
