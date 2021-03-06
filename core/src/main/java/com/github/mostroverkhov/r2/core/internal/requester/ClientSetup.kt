@file:JvmName("ClientSetup")

package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.MimeType
import java.nio.ByteBuffer

fun metaData(metadata: Metadata?): SetupData = SetupData(
        MimeType.dataType,
        MimeType.metadataType,
        emptyBuffer,
        metadataBuffer(metadata))

private fun metadataBuffer(metadata: Metadata?) =
        metadata?.let { metadataCodec.encode(it) } ?: emptyBuffer


data class SetupData internal constructor(val dataType: String,
                                          val metadataType: String,
                                          val data: ByteBuffer,
                                          val metadata: ByteBuffer)

private val metadataCodec = MetadataCodec()
private val emptyBuffer = ByteBuffer.allocate(0)
