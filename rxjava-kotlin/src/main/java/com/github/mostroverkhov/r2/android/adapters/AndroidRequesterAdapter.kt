package com.github.mostroverkhov.r2.android.adapters

import com.github.mostroverkhov.r2.core.internal.requester.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rsocket.android.Payload
import io.rsocket.android.RSocket
import io.rsocket.android.util.PayloadImpl
import org.reactivestreams.Publisher
import java.lang.reflect.Method

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

    override fun resolve(action: Method, err: RuntimeException): Any =
            with(action.returnType) {
                when {
                    typeIs<Completable>() -> Completable.error(err)
                    typeIs<Single<*>>() -> Single.error<Any>(err)
                    typeIs<Flowable<*>>() -> Flowable.error<Any>(err)
                    else -> throw err
                }
            }

    private inline fun <reified T> Class<*>.typeIs()
            = T::class.java == this

    private fun Call.decode(arg: Payload): Any {
        this as RequestCall
        return decodeData(arg.data)
    }

    private fun Call.encode(): Payload {
        this as RequestCall
        return PayloadImpl(encodeData(getArgs().data), encodeMetadata())
    }

    private fun Call.encodePublisher(): Publisher<Payload> {
        this as RequestCall
        var first = true
        return Flowable.fromPublisher(getArgs().data as Publisher<*>)
                .map { t ->
                    val metadata = if (first) {
                        first = false
                        encodeMetadata()
                    } else null
                    PayloadImpl(encodeData(t), metadata)
                }
    }
}