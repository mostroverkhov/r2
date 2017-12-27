# R2
[![Build Status](https://travis-ci.org/mostroverkhov/r2.svg?branch=master)](https://travis-ci.org/mostroverkhov/r2)  

RSocket based RPC for JVM (via [rsocket-java](https://github.com/rsocket/rsocket-java)) and Android (via [rsocket-android](https://github.com/rsocket/rsocket-android)) with pluggable serialization

Supports 4 interaction models: fire-and-forget, request-response, request-stream, request-channel  

### Usage

Given interface

```java
    @Service("svc")
    interface PersonsService {

        @RequestStream("stream")
        Flux<Person> stream(Person person);

        @RequestResponse("response")
        Mono<Person> response(Person person);

        @FireAndForget("fnf")
        Mono<Void> fnf(Person person);

        @RequestChannel("channel")
        Flux<Person> channel(Flux<Person> person);

        @Close
        Mono<Void> close();

        @OnClose
        Mono<Void> onClose();
    }
```

R2 provides `RequesterFactory`
```java
        Mono<RequesterFactory> = new R2Client()
                .connectWith(clientRSocketFactory())
                .metadata(metadata())
                .transport(TcpClientTransport.create(PORT))
                .configureRequester(b -> b.codec(new JacksonDataCodec()));
```

For creating Requesters
```java
        PersonsService svc = requesterFactory.create(PersonsService.class);
```

And `RequestAcceptor` for handling requests
```java
        RequestAcceptor<ConnectionSetupPayload, Mono<RSocket>> acceptor = 
        new JavaAcceptorBuilder()
                .codecs(new Codecs().add(new JacksonDataCodec()))
                .services(ctx -> new Services().add(new PersonServiceHandler()))
                .build();

```

Here, `PersonServiceHandler` implements `PersonsService`

Server part of `RSocket` is started as
```java
      RSocketFactory
                .receive()
                .acceptor(() ->
                        (setup, sendRSocket) -> serverAcceptor.accept(setup)
                ).transport(TcpServerTransport.create(PORT))
                .start()
```

Request methods can have payload (as data - `T`, or `Publisher<T>` for Channel requests), metadata, both, or none. Channel requests must have at least one argument.

### Examples

Runnable example with Client and Server is available [here](https://github.com/mostroverkhov/r2/blob/master/java/src/test/java/com/github/mostroverkhov/r2/java/JavaClientServerExample.java), example of Client for Android is [here](https://github.com/mostroverkhov/r2/blob/master/android/src/test/java/com/github/mostroverkhov/r2/android/AndroidClientExample.kt)
