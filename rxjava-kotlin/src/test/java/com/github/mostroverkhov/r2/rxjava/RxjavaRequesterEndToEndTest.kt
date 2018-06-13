package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec
import com.github.mostroverkhov.r2.core.Codecs
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.Services
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import io.reactivex.Flowable
import io.rsocket.kotlin.DefaultPayload
import io.rsocket.kotlin.Payload
import io.rsocket.kotlin.Setup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer

class RxjavaRequesterEndToEndTest {

    private lateinit var svc: PersonsService
    @Before
    fun setUp() {

        val handlerRSocket = ServerAcceptorBuilder()
                .codecs(Codecs() + JacksonJsonDataCodec())
                .services { _, _ -> Services() + PersonServiceHandler() }
                .build()
                .accept(mockSetupPayload(), DummyRSocket())

        val requesterFactory = handlerRSocket.map {
            RequesterBuilder(it)
                    .codec(JacksonJsonDataCodec())
                    .build()
        }.blockingGet()
        svc = requesterFactory.create()
    }

    @Test(timeout = 5_000)
    fun stream() {
        val persons = svc.stream(Person("john", "doe")).toList().blockingGet()

        assertEquals(1, persons.size)
        val person = persons[0]
        assertEquals("john", person.name)
        assertEquals("doe", person.surname)
    }

    @Test(timeout = 5_000)
    fun response() {
        val person = svc.response(Person("john", "doe")).blockingGet()

        assertTrue(person != null)
        assertEquals("john", person.name)
        assertEquals("doe", person.surname)
    }

    @Test(timeout = 5_000)
    fun fnf() {
        val person = svc.fnf(Person("john", "doe")).blockingGet()
    }

    @Test
    fun channel() {
        val persons = svc.channel(Flowable.just(Person("john", "doe")))
                .toList()
                .blockingGet()

        assertEquals(1, persons.size)
        val person = persons[0]
        assertEquals("john", person.name)
        assertEquals("doe", person.surname)
    }

    @Test
    fun emptyResponse() {
        val person = svc.emptyResponse().blockingGet()
        assertEquals("john", person.name)
        assertEquals("doe", person.surname)
    }

    @Test
    fun onlyMetadataResponse() {
        val metadata = Metadata.Builder().data("foo", "bar".toByteArray(Charsets.UTF_8)).build()
        val person = svc.metadataResponse(metadata).blockingGet()
        assertEquals("john", person.name)
        assertEquals("doe", person.surname)
    }


    @Test(timeout = 5_000, expected = IllegalArgumentException::class)
    fun noAnno() {
        val person = svc.noAnno(Person("john", "doe")).toList().blockingGet()
    }

    @Test(timeout = 5_000, expected = IllegalArgumentException::class)
    fun emptyAnno() {
        svc.emptyAnno(Person("john", "doe"))
                .blockingSubscribe()
    }

    private fun mockSetupPayload(): Setup {
        val md = Metadata.Builder()
                .data("auth", Charsets.UTF_16.encode("secret"))
                .build()
        val mdByteBuffer = MetadataCodec().encode(md)
        return MockSetup("stub", "stub",
        DefaultPayload(ByteBuffer.allocate(0),
                mdByteBuffer))
    }

    private class MockSetup(private val dataMime: String,
                            private val metadataMime: String,
                            private val payload: Payload) : Setup {

        override val data: ByteBuffer
            get() = payload.data

        override val metadata: ByteBuffer
            get() = payload.metadata

        override fun dataMimeType(): String = dataMime

        override fun hasMetadata(): Boolean = payload.hasMetadata()

        override fun metadataMimeType(): String = metadataMime

    }
}