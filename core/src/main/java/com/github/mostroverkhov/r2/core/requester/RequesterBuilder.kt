package com.github.mostroverkhov.r2.core.requester

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.RouteEncoder
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.StringRouteCodec

open class RequesterBuilder {

    private lateinit var dataCodec: DataCodec
    private var routeEncoder = defaultRouteEncoder
    private lateinit var callAdapter: CallAdapter
    private val metadataBuilder = Metadata.RequestBuilder()

    fun codec(dataCodec: DataCodec): RequesterBuilder {
        this.dataCodec = dataCodec
        return this
    }

    fun routeEncoder(routeEncoder: RouteEncoder): RequesterBuilder {
        this.routeEncoder = routeEncoder
        return this
    }

    protected fun adapter(adapter: CallAdapter): RequesterBuilder {
        callAdapter = adapter
        return this
    }

    fun build(): RequesterFactory {
        return RequesterFactory(
                callAdapter,
                RequesterCallResolver(dataCodec,
                        metadataCodec,
                        routeEncoder,
                        metadataBuilder))
    }

    companion object {
        private val defaultRouteEncoder: RouteEncoder = StringRouteCodec().encoder()
        private val metadataCodec = MetadataCodec()
    }
}