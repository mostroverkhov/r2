package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import java.nio.ByteBuffer

class ClientSetup {
    private var metadata: Metadata? = null
    fun metadata(metadata: Metadata): ClientSetup {
        this.metadata = metadata
        return this
    }

    fun <T> setupMetadata(factory: (SetupMetadata) -> T): T = factory(asSetupMetadata())

    private fun asSetupMetadata(): SetupMetadata = SetupMetadata(
            dataType,
            metadataType,
            asBuffer(metadata),
            emptyBuffer)

    private fun asBuffer(metadata: Metadata?): ByteBuffer =
            metadata?.let { metadataCodec.encode(it) }
                    ?: emptyBuffer

    companion object {
        private val metadataCodec = MetadataCodec()
        private val dataType = "application/x.mostroverkhov.r2"
        private val metadataType = "application/x.mostroverkhov.r2"
        private val emptyBuffer = ByteBuffer.allocate(0)
    }
}

data class SetupMetadata(val dataType: String,
                         val metadataType: String,
                         val data:ByteBuffer,
                         val metadata: ByteBuffer)
