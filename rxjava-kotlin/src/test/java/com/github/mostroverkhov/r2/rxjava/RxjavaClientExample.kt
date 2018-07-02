package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.Codecs
import com.github.mostroverkhov.r2.core.Services
import io.rsocket.kotlin.RSocketFactory
import io.rsocket.transport.okhttp.client.OkhttpWebsocketClientTransport
import okhttp3.HttpUrl
import org.junit.Test

class RxjavaClientExample {

    @Test
    fun clientExample() {
        val clientFactory = RSocketFactory
                .connect()
        val md = metadata()
        val url = url()

        val requesterFactory = R2Client()
                .connectWith(clientFactory)
                .metadata(md)
                .configureAcceptor { configurer ->
                    configurer
                            .codecs(Codecs() + JacksonJsonDataCodec())
                            .services { requester ->
                                Services() + SmarterPersonsServiceHandler(requester)
                            }
                }
                .transport(OkhttpWebsocketClientTransport.create(url))
                .start()

        @Suppress("UNUSED_VARIABLE")
        val svc = requesterFactory.map { it.create<PersonsService>() }
    }

    private fun url() = HttpUrl.Builder()
            .host(host)
            .port(port)
            .scheme(scheme)
            .build()

    private fun metadata(): Metadata {
        return Metadata.Builder()
                .auth("foo".toByteArray(Charsets.UTF_8))
                .build()
    }

    companion object {
        const val host = "foo.com"
        const val port = 8081
        const val scheme = "https"
    }
}