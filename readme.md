# R2: RSocket RPC
[![Build Status](https://travis-ci.org/mostroverkhov/r2.svg?branch=master)](https://travis-ci.org/mostroverkhov/r2)  

RSocket based RPC with pluggable serialization (JSON, CBOR, Protocol Buffers and others), 
[java8/reactor](https://github.com/rsocket/rsocket-java) and [kotlin/rxjava2](https://github.com/rsocket/rsocket-android) bindings.

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
    compile 'com.github.mostroverkhov.r2:reactor-java:0.3.0'
```

kotlin/rxjava2
```groovy
    compile 'com.github.mostroverkhov.r2:rxjava-kotlin:0.3.0'
```

serialization
```groovy
        
     /*JSON support with Jackson*/ 
     compile 'com.github.mostroverkhov.r2:codec-jackson:0.3.0'
     
     /*CBOR and Smile support with Jackson-dataformats-binary*/ 
     compile 'com.github.mostroverkhov.r2:codec-jackson-binary:0.3.0'
        
     /*Protocol buffers*/
     compile 'com.github.mostroverkhov.r2:codec-proto:0.3.0'
```

### Usage

Given service interface

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

And `PersonServiceHandler` - implementation of `PersonsService`

R2 provides `RequesterFactory` to create *Requesters* -`RequesterFactory.create(PersonsService.class)`, and `Services`( containing `PersonsServiceHandler()`) - to act as *Responder* for incoming requests. Such Requester and Responder available for each side of connection(*Client* and *Server*).   

`Client` side (Connection initiator) can be constructed as follows:

```java    
   
   Mono<RequesterFactory> requesterFactory =
     new R2Client()
        .connectWith(RSocketFactory.connect())
        /*Passed to Server (Connection Acceptor) as ConnectionContext*/
        .metadata(metadata())
        /*Configure Requester and Responder sides of Client side of Connection*/
        .configureAcceptor(JavaClientServerExample::configureClient)
        .transport(TcpClientTransport.create(PORT))
        .start();
```
Configuration consists of providing `DataCodec` for payloads serialization, and Services to handle incoming requests
from peer. `RequesterFactory` is available to service handlers too

```java
    @NotNull
    private static ClientAcceptorBuilder configureClient(ClientAcceptorBuilder b) {
       return b
         .codecs(new Codecs()
            .add(new JacksonJsonDataCodec()))
        .services(requesterFactory ->
            new Services()
                .add(new PersonServiceHandler()));
  }
```

Requesters can be created as follows

```java
        PersonsService svc = requesterFactory.create(PersonsService.class);
```

`Server` side (Connection Acceptor) 

```java
     Mono<NettyContextCloseable> startedServer =
        new R2Server<NettyContextCloseable>()
            .connectWith(RSocketFactory.receive())
            /*Configure Requester and Responder sides of Server side of Connection*/
            .configureAcceptor(JavaClientServerExample::configureServer)
            .transport(TcpServerTransport.create(PORT))
            .start();
```
Its configuration is symmetric to `Client` and looks like

```java
  private static ServerAcceptorBuilder configureServer(ServerAcceptorBuilder builder) {
    return builder
        /*Jackson Json codec. Also there can be cbor, protobuf etc*/
        .codecs(
            new Codecs()
                .add(new JacksonJsonDataCodec()))
        
        /*ctx is ConnectionContext represents Metadata(key -> value) set by
        Client (Connection initiator) as metadata.*/
        /*RequesterFactory uses first codec provided in Codecs*/
        .services((ctx, requesterFactory) ->
            new Services()
                .add(new RequestingPersonServiceHandler(requesterFactory)));
  }
```
In addition to `RequesterFactory`, `Service Handlers` have access to `ConnectionContext` - initial metadata set by `Client` on connection.

Server is started as
```java
     Mono<NettyContextCloseable> started = serverStart.start()
```
### Requests

Request methods can have Payload (as data - `T`, or `Publisher<T>` for Channel requests), `Metadata`, both, or none. Channel requests must have Payload at least.

### Serialization

`codec-jackson` provides simple JSON serialization. Also, some binary formats (CBOR and Smile) are supported with `codec-jackson-binary` artifact. `codec-proto` provides Protocol Buffers serialization. Custom data codecs can be written by implementing minimalistic `DataCodec` interface.

### Performance

Check [RPC-Thunderdome](https://github.com/mostroverkhov/rpc-thunderdome) project to roughly estimate relative performance of RSocket R2, RSocket Proteus, Grpc & Ratpack

### Examples

Runnable example with Java/Reactor client and server is available [here](https://github.com/mostroverkhov/r2/blob/master/reactor-java/src/test/java/com/github/mostroverkhov/r2/java/JavaClientServerExample.java), example of client for rxJava/kotlin is [here](https://github.com/mostroverkhov/r2/blob/master/rxjava-kotlin/src/test/java/com/github/mostroverkhov/r2/android/AndroidClientExample.kt)

### LICENSE

Copyright 2017 Maksym Ostroverkhov

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
