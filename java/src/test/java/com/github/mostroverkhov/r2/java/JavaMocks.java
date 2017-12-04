package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.contract.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JavaMocks {
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


        Flux<Person> noAnno(Person person);

        @RequestStream
        Flux<Person> emptyAnno(Person person);

        @Close
        Mono<Void> close();

        @OnClose
        Mono<Void> onClose();
    }

    static class PersonServiceHandler implements PersonsService {

        @Override
        public Flux<Person> stream(Person person) {
            return Flux.just(person);
        }

        @Override
        public Mono<Person> response(Person person) {
            return Mono.just(person);
        }

        @Override
        public Mono<Void> fnf(Person person) {
            return Mono.empty();
        }

        @Override
        public Flux<Person> channel(Flux<Person> person) {
            return Flux.from(person).flatMap(p -> Flux.just(p, p));
        }

        @Override
        public Flux<Person> noAnno(Person person) {
            return null;
        }

        @Override
        public Flux<Person> emptyAnno(Person person) {
            return null;
        }

        @Override
        public Mono<Void> close() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> onClose() {
            return Mono.empty();
        }
    }

    static class Person {
        private String name;
        private String surname;

        public Person(String name, String surname) {
            this.name = name;
            this.surname = surname;
        }

        public Person() {
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }
    }
}
