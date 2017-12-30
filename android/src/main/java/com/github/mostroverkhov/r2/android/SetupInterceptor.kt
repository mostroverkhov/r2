package com.github.mostroverkhov.r2.android

import io.reactivex.Flowable
import io.rsocket.android.DuplexConnection
import io.rsocket.android.Frame
import io.rsocket.android.exceptions.InvalidSetupException
import io.rsocket.android.plugins.DuplexConnectionInterceptor
import io.rsocket.android.plugins.DuplexConnectionInterceptor.*
import org.reactivestreams.Publisher

class SetupInterceptor(private val dataMime: String,
                       private val metadataMime: String)
    : DuplexConnectionInterceptor {

    override fun invoke(type: DuplexConnectionInterceptor.Type,
                        conn: DuplexConnection): DuplexConnection =
            if (type == Type.STREAM_ZERO) SetupConnection(conn) else conn

    private inner class SetupConnection(private val source: DuplexConnection)
        : DuplexConnection {

        override fun availability() = source.availability()

        override fun close() = source.close()

        override fun onClose() = source.onClose()

        override fun receive(): Flowable<Frame> {
            return source
                    .receive()
                    .doOnNext { f ->
                        val dataMimeType = Frame.Setup.dataMimeType(f)
                        val metadataMimeType = Frame.Setup.metadataMimeType(f)
                        if (dataMime != dataMimeType
                                || metadataMime != metadataMimeType) {
                            val msg = "Unsupported r2: $dataMimeType : $metadataMimeType"
                            val error = InvalidSetupException(msg)
                            source.sendOne(Frame.Error.from(0, error))
                                    .andThen(source.close())
                                    .subscribe({}, {})

                        }
                    }
        }

        override fun send(frame: Publisher<Frame>) = source.send(frame)
    }
}