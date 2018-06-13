package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.codec.jackson.JacksonJsonDataCodec;
import com.github.mostroverkhov.r2.reactor.RequesterBuilder;
import io.rsocket.AbstractRSocket;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;

public class JavaRequesterCloseTest {


    private MockRSocket mockRSocket;
    private JavaMocks.PersonsService personsService;

    @Before
    public void setUp() throws Exception {

        mockRSocket = new MockRSocket();
        personsService = new RequesterBuilder(mockRSocket)
                .codec(new JacksonJsonDataCodec())
                .build().create(JavaMocks.PersonsService.class);
    }

    @Test
    public void close() throws Exception {
        personsService.close();
        assertEquals(1, mockRSocket.closeCalled);
    }

    @Test
    public void onClose() throws Exception {
        personsService.onClose();
        assertEquals(1, mockRSocket.onCloseCalled);
    }

    class MockRSocket extends AbstractRSocket {
        private int closeCalled;
        private int onCloseCalled;

        @Override
        public Mono<Void> close() {
            closeCalled++;
            return super.close();
        }

        @Override
        public Mono<Void> onClose() {
            onCloseCalled++;
            return super.onClose();
        }
    }

}
