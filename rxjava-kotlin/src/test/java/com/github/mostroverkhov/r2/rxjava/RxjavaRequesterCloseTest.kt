package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec
import com.github.mostroverkhov.r2.rxjava.internal.RequesterBuilder
import io.reactivex.Completable
import io.rsocket.kotlin.util.AbstractRSocket
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RxjavaRequesterCloseTest {

    lateinit var personsService: PersonsService
    lateinit var rsocket: MockRSocket

    @Before
    fun setUp() {

        rsocket = MockRSocket()
        personsService = RequesterBuilder(rsocket)
                .codec(JacksonJsonDataCodec())
                .build()
                .create()
    }

    @Test
    fun close() {
        personsService.close()
        assertEquals(1, rsocket.closedCount())
    }

    @Test()
    fun onClose() {
        personsService.onClose()
        assertEquals(1, rsocket.onClosedCount())
    }

    class MockRSocket : AbstractRSocket() {

        private var closeCalled = 0
        private var onCloseCalled = 0

        fun closedCount() = closeCalled

        fun onClosedCount() = onCloseCalled

        override fun onClose(): Completable {
            onCloseCalled++
            return super.onClose()
        }

        override fun close(): Completable {
            closeCalled++
            return super.close()
        }
    }
}