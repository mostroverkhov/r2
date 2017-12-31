package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.internal.MimeType;
import io.rsocket.DuplexConnection;
import io.rsocket.Frame;
import io.rsocket.exceptions.InvalidSetupException;
import io.rsocket.plugins.DuplexConnectionInterceptor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.rsocket.Frame.Setup.dataMimeType;
import static io.rsocket.Frame.Setup.metadataMimeType;

class SetupInterceptor implements DuplexConnectionInterceptor {
    private final String dataMime;
    private final String metadataMime;

    public SetupInterceptor(MimeType mimeType) {
        this.dataMime = mimeType.getDataType();
        this.metadataMime = mimeType.getMetadataType();
    }

    @Override
    public DuplexConnection apply(Type type, DuplexConnection connection) {
        return type == Type.STREAM_ZERO
                ? new SetupConnection(connection)
                : connection;
    }

    private class SetupConnection implements DuplexConnection {
        private final DuplexConnection source;

        public SetupConnection(DuplexConnection source) {
            this.source = source;
        }

        @Override
        public Mono<Void> send(Publisher<Frame> frame) {
            return source.send(frame);
        }

        @Override
        public Mono<Void> sendOne(Frame frame) {
            return source.sendOne(frame);
        }

        @Override
        public Flux<Frame> receive() {
            return source
                    .receive()
                    .doOnNext(f -> {
                        String dataMimeType = dataMimeType(f);
                        String metadataMimeType = metadataMimeType(f);
                        if (!dataMime.equals(dataMimeType) ||
                                !metadataMime.equals(metadataMimeType)) {

                            String msg = errorMsg(dataMimeType, metadataMimeType);

                            InvalidSetupException error = new InvalidSetupException(msg);
                            source.sendOne(Frame.Error.from(0, error))
                                    .then(source.close())
                                    .subscribe(ignore -> {
                                    }, ignoreErr -> {
                                    });
                        }
                    });
        }

        @Override
        public double availability() {
            return source.availability();
        }

        @Override
        public Mono<Void> close() {
            return source.close();
        }

        @Override
        public Mono<Void> onClose() {
            return source.onClose();
        }

        @NotNull
        private String errorMsg(String dataMimeType, String metadataMimeType) {
            return new StringBuilder()
                    .append("Unsupported r2: ")
                    .append(dataMimeType)
                    .append(" : ")
                    .append(metadataMimeType).toString();
        }
    }
}
