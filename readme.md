# R2: RSocket RPC
[![Build Status](https://travis-ci.org/mostroverkhov/r2.svg?branch=master)](https://travis-ci.org/mostroverkhov/r2)  

Flexible RPC which enables Reactive Streams interactions over network boundary (TCP, WebSockets, etc)
with pluggable serialization (JSON, CBOR, Protocol Buffers etc), and [Reactor](https://github.com/rsocket/rsocket-java) & [RxJava](https://github.com/rsocket/rsocket-kotlin) bindings.

Built on top of [RSocket](http://rsocket.io/) - binary application protocol bringing Reactive Streams semantics
to network communications. Check [FAQ](https://github.com/rsocket/rsocket/blob/master/FAQ.md) for more info.

Supports 4 interaction models: 
* `Fire-and-forget`
```java
Publisher<Void> fireAndForget(Request request)
```
* `Request-Response`
```java
Publisher<Response> response(Request request) 
```
* `Request-Stream`
```java
Publisher<Response> stream(Request request)
```
* `Request-Channel`
```java
Publisher<Response> channel(Publisher<Request> request)
```
* `Metadata` passing along every of above interactions  
```java
Publisher<Response> response(Request request, Metadata metadata)
```
### Build and Binaries

The project is released on [jitpack](https://jitpack.io/#mostroverkhov/r2)
```groovy
   repositories {
      maven { url 'https://jitpack.io' }
   }
```

Reactor bindings
```groovy
    compile 'com.github.mostroverkhov.r2:reactor-java:0.5.0'
```

RxJava bindings
```groovy
    compile 'com.github.mostroverkhov.r2:rxjava-kotlin:0.5.0'
```

Serialization: JSON, CBOR, Smile, Protocol Buffers
```groovy
        
     /*JSON support with Jackson*/ 
     compile 'com.github.mostroverkhov.r2:codec-jackson:0.5.0'
     
     /*CBOR and Smile support with Jackson-dataformats-binary*/ 
     compile 'com.github.mostroverkhov.r2:codec-jackson-binary:0.5.0'
        
     /*Protocol buffers*/
     compile 'com.github.mostroverkhov.r2:codec-proto:0.5.0'
```

### Usage

RSocket is symmetric protocol, so each peer: `Client` - initiator of connection,   
and `Server` - acceptor of connection, has `Requester` to perform requests,  
 and `Responder` to accept requests from peer.

R2 service is interface with interactions defined in terms of Reactive Streams `Publisher`

```java
    @Service("svc")
    interface PersonsService {

        @RequestStream("stream")
        Publisher<Person> stream(Person person);

        @RequestResponse("response")
        Publisher<Person> response(Person person);

        @FireAndForget("fnf")
        Publisher<Void> fnf(Person person);

        @RequestChannel("channel")
        Publisher<Person> channel(Publisher<Person> person);

        @Close
        Publisher<Void> close();

        @OnClose
        Publisher<Void> onClose();
    }
```

Library generates reactive library specific definitions, which for `Reactor` look as follows.
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
Let `PersonServiceHandler` be implementation of above `PersonsService`.

R2 provides `RequesterFactory` to create `Requesters`, 
```java
PersonService personService = requesterFactory.create(PersonsService.class)
``` 
and accepts handlers (e.g. `PersonsServiceHandler`) through its `Services` API.  
Set of handlers act as peer `Responder` for incoming requests.   

`Client` (connection initiator) can be constructed as follows:

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
Configuration consists of providing `DataCodec` for payloads serialization, and `Services` to handle incoming requests
from peer. `RequesterFactory` is available to `Services` handlers too

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
Its configuration is symmetric with `Client`, and looks like

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
In addition to `RequesterFactory`, `Service` handlers have access to `ConnectionContext` - initial metadata sent by `Client` on connection.

Server can be started as follows
```java
     Mono<NettyContextCloseable> started = serverStart.start()
```
### Service methods

Service methods can have Payload (as data - `T`, or `Publisher<T>` for Channel requests), `Metadata`, both, or none. Channel requests must have Payload at least.

### Serialization

`codec-jackson` provides simple JSON serialization. Also, some binary formats (CBOR and Smile) are supported with `codec-jackson-binary` artifact. `codec-proto` provides Protocol Buffers serialization. Custom data codecs can be written by implementing minimalistic `DataCodec` interface.

### Monitoring

R2 Reactor supports multiple monitoring systems with [Micrometer](https://micrometer.io/) library.  
Metrics are provided for `RSocket` interactions, `DuplexConnection` frames and `R2` interactions (service name, method name). 

### Performance

Check [RPC-Thunderdome](https://github.com/mostroverkhov/rpc-thunderdome) project to roughly estimate relative performance of RSocket R2, RSocket Proteus, GRPC & Ratpack

### Examples

Runnable example with Reactor/Java client and server is available [here](https://github.com/mostroverkhov/r2/blob/master/example/src/main/java/com/github/mostroverkhov/r2/example/ReactorClientServerExample.java), example of client for RxJava2/Kotlin is [here](https://github.com/mostroverkhov/r2/blob/master/rxjava-kotlin/src/test/java/com/github/mostroverkhov/r2/rxjava/RxjavaClientExample.kt)

### LICENSE

Copyright 2018 Maksym Ostroverkhov

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
