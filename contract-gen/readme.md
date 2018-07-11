
### Contract-generator

Annotation processor to convert generic service contract defined in   
terms of Reactive Streams `Publisher` into service definition with  
 reactive library specific types: Rxjava & Reactor

E.g. for service contract

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
``` 
e.g. utility can generate following `Reactor` service definition

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

### Configuration

Annotation processor is configured with compiler arguments

`r2.gen.enabled`: boolean, true if service definitions must be generated 
`r2.gen.platform`: reactive library, `rxjava` | `reactor`  
`r2.gen.package`: package (serves also as file path) for generated service definition

Configuration example for Gradle is available in `contract-gen-test` module

  