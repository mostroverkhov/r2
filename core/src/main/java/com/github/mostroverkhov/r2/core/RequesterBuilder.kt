package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.ServiceMethodEncoder
import com.github.mostroverkhov.r2.core.internal.StringServiceMethodCodec
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver

open class RequesterBuilder protected constructor() {

    private lateinit var dataCodec: DataCodec
    private var routeEncoder = defaultServiceMethodEncoder
    private lateinit var callAdapter: CallAdapter

    fun codec(dataCodec: DataCodec): RequesterBuilder {
        this.dataCodec = dataCodec
        return this
    }

    protected fun adapter(adapter: CallAdapter): RequesterBuilder {
        callAdapter = adapter
        return this
    }

    fun build(): RequesterFactory {
        return RequesterFactory(
                callAdapter,
                RequesterCallResolver(
                        dataCodec,
                        metadataCodec,
                        routeEncoder))
    }

    companion object {
        private val defaultServiceMethodEncoder: ServiceMethodEncoder =
                StringServiceMethodCodec()
                        .encoder()
        private val metadataCodec = MetadataCodec()
    }
}