package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.ServiceMethodEncoder
import com.github.mostroverkhov.r2.core.internal.PathServiceMethodCodec
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver
import com.github.mostroverkhov.r2.core.internal.requester.RequesterFactoryProxy

open class CoreRequesterBuilder protected constructor() {

    private lateinit var dataCodec: DataCodec
    private var serviceMethodEncoder = defaultServiceMethodEncoder
    private lateinit var callAdapter: CallAdapter

    fun codec(dataCodec: DataCodec): CoreRequesterBuilder {
        this.dataCodec = dataCodec
        return this
    }

    protected fun adapter(adapter: CallAdapter): CoreRequesterBuilder {
        callAdapter = adapter
        return this
    }

    fun build(): RequesterFactory {
        return RequesterFactoryProxy(
                callAdapter,
                RequesterCallResolver(
                        dataCodec,
                        metadataCodec,
                        serviceMethodEncoder))
    }

    companion object {
        private val defaultServiceMethodEncoder: ServiceMethodEncoder =
                PathServiceMethodCodec()
                        .encoder()
        private val metadataCodec = MetadataCodec()
    }
}