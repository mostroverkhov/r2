package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.internal.MimeType
import io.reactivex.Flowable
import io.rsocket.android.DuplexConnection
import io.rsocket.android.Frame
import io.rsocket.android.exceptions.InvalidSetupException
import io.rsocket.android.plugins.DuplexConnectionInterceptor
import io.rsocket.android.plugins.DuplexConnectionInterceptor.*
import org.reactivestreams.Publisher

internal class SetupInterceptor(private val mimeType: MimeType)
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
                        if (mimeType.dataType != dataMimeType
                                || mimeType.metadataType != metadataMimeType) {
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