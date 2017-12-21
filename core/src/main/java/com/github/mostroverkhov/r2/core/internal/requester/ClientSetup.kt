@file:JvmName("ClientSetup")

package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import java.nio.ByteBuffer

fun setupData(metadata: Metadata): SetupData = SetupData(
        dataType,
        metadataType,
        emptyBuffer,
        metadataCodec.encode(metadata))


data class SetupData(val dataType: String,
                     val metadataType: String,
                     val data: ByteBuffer,
                     val metadata: ByteBuffer)

private val metadataCodec = MetadataCodec()
private val dataType = "application/x.mostroverkhov.r2"
private val metadataType = "application/x.mostroverkhov.r2"
private val emptyBuffer = ByteBuffer.allocate(0)
