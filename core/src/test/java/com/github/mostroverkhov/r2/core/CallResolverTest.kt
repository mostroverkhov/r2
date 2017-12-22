package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.StringRouteCodec
import com.github.mostroverkhov.r2.core.internal.requester.CloseCall
import com.github.mostroverkhov.r2.core.internal.requester.Interaction
import com.github.mostroverkhov.r2.core.internal.requester.RequestCall
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver
import com.github.mostroverkhov.r2.core.requester.RequesterFactory
import org.junit.Assert.*
import org.junit.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class CallResolverTest {
    private val dataCodec = MockCodec()
    private val mdCodec = MetadataCodec()
    private val routeEncoder = StringRouteCodec().encoder()

    private val callResolver = RequesterCallResolver(
            dataCodec,
            mdCodec,
            routeEncoder)

    private val callHandler = RequesterFactory.CallHandler(callResolver, Svc::class.java)

    @Test
    fun fnf() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.FNF, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("fnf", call.method)
            assertEquals(Request("fnf"), call.args.data)
            assertEquals(Void::class.java, call.responsePayloadType)
            Pub<Void>()
        }.fnf(Request("fnf"))
    }

    @Test
    fun response() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("rep", call.method)
            assertEquals(Request("rep"), call.args.data)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }.response(Request("rep"))
    }

    @Test
    fun stream() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.STREAM, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("stream", call.method)
            assertEquals(Request("stream"), call.args.data)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }.stream(Request("stream"))
    }

    @Test
    fun channel() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.CHANNEL, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("channel", call.method)
            assertTrue(call.args.data is Publisher<*>)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }.channel(Pub(Request("channel")))
    }

    @Test
    fun metadataResponse() {
        val metadata = Metadata.Builder()
                .data("foo", "bar".toByteArray(Charsets.UTF_8))
                .build()

        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("metadata", call.method)
            assertNull(call.args.data)
            assertNotNull(call.args.metadata)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }.metadataResponse(metadata)
    }

    @Test
    fun emptyResponse() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("empty", call.method)
            assertNull(call.args.data)
            assertNull(call.args.metadata)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }.emptyResponse()
    }


    @Test
    fun close() {
        callHandler.handleWith { call ->
            assertTrue(call is CloseCall)
            assertEquals(Interaction.CLOSE, call.interaction)
            Pub<Void>()
        }.close()
    }

    @Test
    fun onClose() {
        callHandler.handleWith { call ->
            assertTrue(call is CloseCall)
            assertEquals(Interaction.ONCLOSE, call.interaction)
            Pub<Void>()
        }.onClose()
    }

    @Test(expected = IllegalArgumentException::class)
    fun noSvcAnno() {
        RequesterFactory.CallHandler(callResolver, SvcNoAnno::class.java).handleWith { Pub<Void>() }
                .fnf(Request("fnf"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noInteraction() {
        callHandler.handleWith { Pub<Void>() }.noInteraction(Pub(Request("channel")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noMethodName() {
        callHandler.handleWith { Pub<Void>() }.noMethodName(Pub(Request("channel")))
    }

    private class Pub<T>(internal val request: Request?) : Publisher<T> {

        constructor() : this(null)

        override fun subscribe(s: Subscriber<in T>?) {
        }
    }

    private interface SvcNoAnno {

        @FireAndForget("fnf")
        fun fnf(arg: Request): Publisher<Void>
    }

    @Service("svc")
    private interface Svc {

        @FireAndForget("fnf")
        fun fnf(arg: Request): Publisher<Void>

        @RequestResponse("rep")
        fun response(arg: Request): Publisher<Response>

        @RequestStream("stream")
        fun stream(arg: Request): Publisher<Response>

        @RequestChannel("channel")
        fun channel(arg: Publisher<Request>): Publisher<Response>

        fun noInteraction(arg: Publisher<Request>): Publisher<Response>

        @RequestChannel
        fun noMethodName(arg: Publisher<Request>): Publisher<Response>

        @RequestResponse("metadata")
        fun metadataResponse(metadata: Metadata): Publisher<Response>

        @RequestResponse("empty")
        fun emptyResponse(): Publisher<Response>

        @Close
        fun close(): Publisher<Void>

        @OnClose
        fun onClose(): Publisher<Void>
    }

    data class Request(val req: String)

    data class Response(val resp: String)
}