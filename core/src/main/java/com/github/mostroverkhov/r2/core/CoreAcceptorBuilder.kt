package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.ServiceMethodDecoder
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.PathServiceMethodCodec
import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver

@Suppress("UNCHECKED_CAST")
abstract class CoreAcceptorBuilder<
        RequesterRSocket,
        HandlerRSocket,
        T : CoreAcceptorBuilder<RequesterRSocket, HandlerRSocket, T>>
internal constructor() {
    private var codecReader: CodecReader? = null
    private val serviceMethodDecoder = defaultServiceMethodDecoder

    fun codecs(codecReader: CodecReader): T {
        this.codecReader = codecReader
        return this as T
    }

    fun codecs(): CodecReader = notNull(codecReader)

    internal fun createTargetResolver(services: ServiceReader) =
            ResponderTargetResolver(
                    services,
                    metadataCodec,
                    serviceMethodDecoder(notNull(codecReader)))


    internal fun createRequesterFactory(rSocket: RequesterRSocket,
                                        builder: (RequesterRSocket) -> CoreRequesterBuilder) =
            builder(rSocket)
                    .codec(notNull(codecReader).primary())
                    .build()

    companion object {
        private var defaultServiceMethodDecoder: (CodecReader) -> ServiceMethodDecoder =
                PathServiceMethodCodec()::decoder

        internal val metadataCodec = MetadataCodec()

        private fun notNull(codecReader: CodecReader?): CodecReader {
            return codecReader
                    ?: throw IllegalStateException("Codecs were not set")
        }
    }
}