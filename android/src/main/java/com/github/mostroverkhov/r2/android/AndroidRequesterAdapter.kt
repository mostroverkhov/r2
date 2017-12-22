package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.internal.requester.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rsocket.android.Payload
import io.rsocket.android.RSocket
import io.rsocket.android.util.PayloadImpl
import org.reactivestreams.Publisher

internal class AndroidRequesterAdapter(private val rSocket: RSocket) : CallAdapter {

    override fun adapt(call: Call): Any {

        return when (call.interaction) {

            Interaction.CHANNEL -> Flowable.defer {
                rSocket.requestChannel(call.encodePublisher())
                        .map { call.decode(it) }
            }

            Interaction.RESPONSE -> Single.defer {
                rSocket.requestResponse(call.encode())
                        .map { call.decode(it) }
            }

            Interaction.STREAM -> Flowable.defer {
                rSocket.requestStream(call.encode())
                        .map { call.decode(it) }
            }

            Interaction.FNF -> Completable.defer {
                rSocket.fireAndForget(call.encode())
            }

            Interaction.CLOSE -> rSocket.close()

            Interaction.ONCLOSE -> rSocket.onClose()
        }
    }

    private fun Call.decode(arg: Payload): Any {
        this as RequestCall
        return decodeData(arg.data)
    }

    private fun Call.encode(): Payload {
        this as RequestCall
        return PayloadImpl(encodeData(args.data), encodeMetadata())
    }

    private fun Call.encodePublisher(): Publisher<Payload> {
        this as RequestCall
        var first = true
        return Flowable.fromPublisher(args.data as Publisher<*>)
                .map { t ->
                    val metadata = if (first) {
                        first = false
                        encodeMetadata()
                    } else null
                    PayloadImpl(encodeData(t), metadata)
                }
    }
}