package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec
import com.github.mostroverkhov.r2.core.Metadata
import io.rsocket.android.RSocketFactory
import io.rsocket.transport.okhttp.client.OkhttpWebsocketClientTransport
import okhttp3.HttpUrl
import org.junit.Test

class AndroidClientExample {

    @Test
    fun clientExample() {
        val clientFactory = RSocketFactory
                .connect()
                .keepAlive()
        val md = metadata()
        val url = url()

        val requesterFactory = R2Client()
                .connectWith(clientFactory)
                .metadata(md)
                .configureRequester { it.codec(JacksonJsonDataCodec()) }
                .transport(OkhttpWebsocketClientTransport.create(url))
                .start()


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
        val host = "foo.com"
        val port = 8081
        val scheme = "https"
    }
}