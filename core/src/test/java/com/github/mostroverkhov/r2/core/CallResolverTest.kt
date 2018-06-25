package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.PathServiceMethodCodec
import com.github.mostroverkhov.r2.core.internal.requester.*
import org.junit.Assert.*
import org.junit.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.lang.reflect.Method

class CallResolverTest {
    private val dataCodec = MockCodec()
    private val mdCodec = MetadataCodec()
    private val svcMethodEncoder = PathServiceMethodCodec().encoder()

    private val callResolver = RequesterCallResolver(
            dataCodec,
            mdCodec,
            svcMethodEncoder)

    private val callHandler = RequesterFactoryProxy.CallHandler(callResolver, Svc::class.java)

    @Test
    fun fnf() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.FNF, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("fnf", call.method)
            assertEquals(Request("fnf"), call.params().data)
            assertEquals(Void::class.java, call.responsePayloadType)
            Pub<Void>()
        }).fnf(Request("fnf"))
    }

    @Test
    fun response() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("rep", call.method)
            assertEquals(Request("rep"), call.params().data)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }).response(Request("rep"))
    }

    @Test
    fun stream() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.STREAM, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("stream", call.method)
            assertEquals(Request("stream"), call.params().data)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }).stream(Request("stream"))
    }

    @Test
    fun channel() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.CHANNEL, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("channel", call.method)
            assertTrue(call.params().data is Publisher<*>)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }).channel(Pub(Request("channel")))
    }

    @Test
    fun metadataResponse() {
        val metadata = Metadata.Builder()
                .data("foo", "bar".toByteArray(Charsets.UTF_8))
                .build()

        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("metadata", call.method)
            assertNull(call.params().data)
            assertNotNull(call.params().metadata)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }).metadataResponse(metadata)
    }

    @Test
    fun emptyResponse() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is RequesterCall)
            call as RequesterCall
            assertEquals(Interaction.RESPONSE, call.interaction)
            assertEquals("svc", call.service)
            assertEquals("empty", call.method)
            assertNull(call.params().data)
            assertNull(call.params().metadata)
            assertEquals(Response::class.java, call.responsePayloadType)
            Pub<Response>()
        }).emptyResponse()
    }


    @Test
    fun close() {
        callHandler.handleWith(Adapter { call ->
            assertTrue(call is TerminationCall)
            assertEquals(Interaction.CLOSE, call.interaction)
            Pub<Void>()
        }).close()
    }

    @Test
    fun onClose() {
        callHandler.handleWith(Adapter
        { call ->
            assertTrue(call is TerminationCall)
            assertEquals(Interaction.ONCLOSE, call.interaction)
            Pub<Void>()
        }).onClose()
    }

    @Test(expected = IllegalArgumentException::class)
    fun noSvcAnno() {
        RequesterFactoryProxy.CallHandler(callResolver, SvcNoAnno::class.java)
                .handleWith(Adapter { Pub<Void>() })
                .fnf(Request("fnf"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noInteraction() {
        callHandler.handleWith(Adapter { Pub<Void>() })
                .noInteraction(Pub(Request("channel")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun noMethodName() {
        callHandler.handleWith(Adapter { Pub<Void>() })
                .noMethodName(Pub(Request("channel")))
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

    private class Adapter(private val adapter: (Call) -> Any) : CallAdapter {

        override fun adapt(call: Call) = adapter(call)

        override fun resolve(action: Method, err: RuntimeException): Any = throw err
    }
}