# R2
[![Build Status](https://travis-ci.org/mostroverkhov/r2.svg?branch=master)](https://travis-ci.org/mostroverkhov/r2)  

RSocket based RPC with pluggable serialization (jackson, cbor, protobuf and others), 
Java8/Reactor (with [rsocket-java](https://github.com/rsocket/rsocket-java)) and Kotlin/RxJava2 (with [rsocket-android](https://github.com/rsocket/rsocket-android)) bindings.

[RSocket](http://rsocket.io/) is binary application protocol bringing Reactive-Streams semantics
to network communications. Check [FAQ](https://github.com/rsocket/rsocket/blob/master/FAQ.md) for more info.

Supports 4 interaction models: `fire-and-forget`, `request-response`, `request-stream`, `request-channel`,
and `Metadata` passing - for connection setup and above interactions.  

### Build and Binaries

The project is released on [jitpack](https://jitpack.io/#mostroverkhov/r2)
```groovy
   repositories {
      maven { url 'https://jitpack.io' }
   }
```

java8/reactor
```groovy
    compile 'com.github.mostroverkhov.r2:java:0.1'
```

kotlin/rxjava2/android
```groovy
    compile 'com.github.mostroverkhov.r2:android:0.1'
```

data codecs
```groovy
        
     /*Json support. CBOR and others are available with jackson-dataformat-binary*/ 
     compile 'com.github.mostroverkhov.r2:codec-jackson:0.1'
        
     /*Protocol buffers*/
     compile 'com.github.mostroverkhov.r2:codec-proto:0.1'
```

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
                                      .connectWith(RSocketFactory.connect())
                                       /*Passed to Server (Connection Acceptor) as ConnectionContext*/
                                      .metadata(metadata())
                                      .configureRequester(b -> b.codec(new JacksonDataCodec()))
                                      .transport(TcpClientTransport.create(PORT))
                                      .start();
```

For creating Requesters
```java
        PersonsService svc = requesterFactory.create(PersonsService.class);
```

And `R2Server` for handling requests
```java
        Mono<NettyContextCloseable> started = new R2Server<NettyContextCloseable>()
                .connectWith(RSocketFactory.receive())
                /*Configure Responder RSocket (acceptor) of server side of Connection.
                  Requester RSocket is not exposed yet*/
                .configureAcceptor(JavaClientServerExample::configureAcceptor)
                .transport(TcpServerTransport.create(PORT))
                .start();

    @NotNull
    private static JavaAcceptorBuilder configureAcceptor(JavaAcceptorBuilder builder) {
        return builder
                /*Jackson codec. Also there can be cbor, protobuf etc*/
                .codecs(new Codecs().add(new JacksonDataCodec()))
                /*ConnectionContext represents Metadata(key -> value) set by Client (Connection initiator)
                as metadata*/
                .services(ctx -> new Services().add(new PersonServiceHandler()));
    }

```

Here, `PersonServiceHandler` implements `PersonsService`

Server part of `RSocket` is started as
```java
     Mono<NettyContextCloseable> started = serverStart.start()
```

Request methods can have payload (as data - `T`, or `Publisher<T>` for Channel requests), `Metadata`, both, or none. Channel requests must have payload at least.

### Serialization

`codec-jackson` provides JSON serialization. Also, some binary formats (cbor, avro and others) are supported with [jackson-dataformat-binary](https://github.com/FasterXML/jackson-dataformats-binary). `codec-proto` provides Protobuf serialization. Custom data codecs can be easily built by implementing minimalistic `DataCodec` interface.

### Examples

Runnable example with Java / Reactor Client and Server is available [here](https://github.com/mostroverkhov/r2/blob/master/java/src/test/java/com/github/mostroverkhov/r2/java/JavaClientServerExample.java), example of Client for Kotlin / RxJava2 is [here](https://github.com/mostroverkhov/r2/blob/master/android/src/test/java/com/github/mostroverkhov/r2/android/AndroidClientExample.kt)

### LICENSE

Copyright 2017 Maksym Ostroverkhov

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
