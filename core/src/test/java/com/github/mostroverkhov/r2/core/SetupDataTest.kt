package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.requester.setupData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupDataTest {

    @Test
    fun setup() {
        val metadata = Metadata.Builder()
                .data("key", "value".toByteArray(Charsets.UTF_8))
                .build()

        val setupData = setupData(metadata)
        assertEquals(setupData.dataType, "application/x.mostroverkhov.r2")
        assertEquals(setupData.metadataType, "application/x.mostroverkhov.r2")
        assertEquals(0, setupData.data.remaining())
        assertTrue(setupData.metadata.remaining() > 0)
    }
}