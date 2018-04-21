package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.requester.metaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupDataTest {

    @Test
    fun setup() {
        val metadata = Metadata.Builder()
                .data("key", "value".toByteArray(Charsets.UTF_8))
                .build()

        val setupData = metaData(metadata)
        assertTrue(setupData.dataType.startsWith("application/x.mostroverkhov.r2"))
        assertTrue(setupData.metadataType.startsWith("application/x.mostroverkhov.r2"))
        assertEquals(0, setupData.data.remaining())
        assertTrue(setupData.metadata.remaining() > 0)
    }
}