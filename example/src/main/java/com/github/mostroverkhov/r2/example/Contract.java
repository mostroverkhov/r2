package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.core.Metadata;
import com.github.mostroverkhov.r2.core.RequesterFactory;
import com.github.mostroverkhov.r2.core.contract.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Contract {
  @Service("svc")
  interface PersonsService {

    @RequestStream("stream")
    Flux<Person> stream(Person person);

    @RequestResponse("response")
    Mono<Person> response(Person person, Metadata metadata);

    @RequestResponse("responseEmpty")
    Mono<Person> responseEmpty();

    @RequestResponse("responseMetadata")
    Mono<Person> responseMetadata(Metadata metadata);

    @FireAndForget("fnf")
    Mono<Void> fnf(Person person, Metadata metadata);

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

  static class RequestingPersonServiceHandler
      extends PersonServiceHandler
      implements PersonsService {
    private final PersonsService personsService;

    public RequestingPersonServiceHandler(String tag,
                                          RequesterFactory requesterFactory) {
      super(tag);
      this.personsService = createPersonsService(requesterFactory);
    }

    @Override
    public Flux<Person> channel(Flux<Person> person) {
      return request().thenMany(super.channel(person));
    }

    private PersonsService createPersonsService(
        RequesterFactory requesterFactory) {
      return requesterFactory.create(PersonsService.class);
    }

    private Mono<Void> request() {
      return personsService
          .response(
              new Person("johanna", "doe"),
              new Metadata.Builder().build()).then();
    }
  }

  static class PersonServiceHandler implements PersonsService {

    private final String tag;

    public PersonServiceHandler(String tag) {
      this.tag = tag;
    }

    @Override
    public Flux<Person> stream(Person person) {
      return Flux.just(withTag(person));
    }

    @Override
    public Mono<Person> response(Person person, Metadata metadata) {
      return Mono.just(withTag(person)).doOnNext(System.out::println);
    }

    @Override
    public Mono<Person> responseEmpty() {
      return Mono.just(withTag(responsePerson()));
    }

    @Override
    public Mono<Person> responseMetadata(Metadata metadata) {
      return Mono.just(withTag(responsePerson()));
    }

    @Override
    public Mono<Void> fnf(Person person, Metadata metadata) {
      return Mono.empty();
    }

    @Override
    public Flux<Person> channel(Flux<Person> person) {
      return Flux.from(person).flatMap(p -> Flux.just(withTag(p), withTag(p)));
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

    @NotNull
    private Person responsePerson() {
      return new Person("john", "doe");
    }


    private Person withTag(Person person) {
      return new Person(
          withTag(person.getName()),
          withTag(person.getSurname()));
    }

    private String withTag(String str) {
      return tag.isEmpty() ? str : tag + " " + str;
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

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Person person = (Person) o;

      if (!name.equals(person.name)) return false;
      return surname.equals(person.surname);
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + surname.hashCode();
      return result;
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
