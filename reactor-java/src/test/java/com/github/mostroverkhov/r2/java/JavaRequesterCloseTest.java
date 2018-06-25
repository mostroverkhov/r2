package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.reactor.internal.RequesterBuilder;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import io.rsocket.AbstractRSocket;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class JavaRequesterCloseTest {


    private MockRSocket mockRSocket;
    private JavaMocks.PersonsService personsService;

    @Before
    public void setUp() throws Exception {
        List<InteractionsInterceptor> empty = Collections.emptyList();
        mockRSocket = new MockRSocket();
        personsService = new RequesterBuilder(mockRSocket, empty)
                .codec(new JacksonJsonDataCodec())
                .build().create(JavaMocks.PersonsService.class);
    }

    @Test
    public void close() throws Exception {
        personsService.close().subscribe();
        assertEquals(1, mockRSocket.closeCalled);
    }

    @Test
    public void onClose() throws Exception {
        personsService.onClose().subscribe();
        assertEquals(1, mockRSocket.onCloseCalled);
    }

    class MockRSocket extends AbstractRSocket {
        private int closeCalled;
        private int onCloseCalled;

        @Override
        public void dispose() {
            closeCalled++;
            super.dispose();
        }

        @Override
        public Mono<Void> onClose() {
            onCloseCalled++;
            return super.onClose();
        }
    }

}
