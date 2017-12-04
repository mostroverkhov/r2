package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.StringRouteCodec
import com.github.mostroverkhov.r2.core.internal.requester.CloseCall
import com.github.mostroverkhov.r2.core.internal.requester.Interaction
import com.github.mostroverkhov.r2.core.internal.requester.RequestCall
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver
import com.github.mostroverkhov.r2.core.requester.RequesterFactory
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class CallResolverTest {
    private val dataCodec = MockCodec()
    private val mdCodec = MetadataCodec()
    private val routeEncoder = StringRouteCodec().encoder()
    private val mdReqBuilder = Metadata.RequestBuilder()

    private val callResolver = RequesterCallResolver(
            dataCodec,
            mdCodec,
            routeEncoder,
            mdReqBuilder)

    private val callHandler = RequesterFactory.CallHandler(callResolver, Svc::class.java)

    @Test
    fun fnf() {
        callHandler.handleWith { call ->
            assertTrue(call is RequestCall)
            call as RequestCall
            assertEquals(Interaction.FNF, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("fnf", call.method)
            assertEquals(Request("fnf"), call.arg)
            assertEquals(Void::class.java, call.responsePayloadType)
            assertTrue(call.metadataFactory(call).hasRoute())
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
            assertEquals(Request("rep"), call.arg)
            assertEquals(Response::class.java, call.responsePayloadType)
            assertTrue(call.metadataFactory(call).hasRoute())
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
            assertEquals(Request("stream"), call.arg)
            assertEquals(Response::class.java, call.responsePayloadType)
            assertTrue(call.metadataFactory(call).hasRoute())
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
            assertTrue(call.arg is Publisher<*>)
            assertEquals(Response::class.java, call.responsePayloadType)
            assertTrue(call.metadataFactory(call).hasRoute())
            Pub<Response>()
        }.channel(Pub(Request("channel")))
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

        @Close
        fun close(): Publisher<Void>

        @OnClose
        fun onClose(): Publisher<Void>
    }

    data class Request(val req: String)

    data class Response(val resp: String)
}