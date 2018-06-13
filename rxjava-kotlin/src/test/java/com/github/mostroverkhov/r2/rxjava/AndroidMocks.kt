package com.github.mostroverkhov.r2.rxjava

import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.RequesterFactory
import com.github.mostroverkhov.r2.core.contract.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rsocket.kotlin.util.AbstractRSocket

@Service("svc")
interface PersonsService {

    @RequestStream("stream")
    fun stream(person: Person): Flowable<Person>

    @RequestResponse("response")
    fun response(person: Person): Single<Person>

    @FireAndForget("fnf")
    fun fnf(person: Person): Completable

    @RequestChannel("channel")
    fun channel(person: Flowable<Person>): Flowable<Person>

    @RequestResponse("emptyResponse")
    fun emptyResponse(): Single<Person>

    @RequestResponse("metadataResponse")
    fun metadataResponse(metadata: Metadata): Single<Person>

    fun noAnno(person: Person): Flowable<Person>

    @RequestStream
    fun emptyAnno(person: Person): Flowable<Person>

    @Close
    fun close(): Completable

    @OnClose
    fun onClose(): Completable
}

class SmarterPersonsServiceHandler(requesterFactory: RequesterFactory)
    : PersonServiceHandler(),
        PersonsService {
    private val personsService = requesterFactory
            .create<PersonsService>()

    override fun fnf(person: Person): Completable {
        return personsService.fnf(person).andThen(super.fnf(person))
    }
}

open class PersonServiceHandler : PersonsService {
    override fun emptyResponse(): Single<Person> {
        return Single.just(Person("john", "doe"))
    }

    override fun metadataResponse(metadata: Metadata): Single<Person> {
        return Single.just(Person("john", "doe"))
    }

    override fun close(): Completable {
        return Completable.complete()
    }

    override fun onClose(): Completable {
        return Completable.complete()
    }

    override fun stream(person: Person): Flowable<Person> = Flowable.just(person.copy())

    override fun response(person: Person): Single<Person> = Single.just(person.copy())

    override fun fnf(person: Person): Completable = Completable.complete()

    override fun channel(person: Flowable<Person>) = Flowable.fromPublisher(person).map { it.copy() }

    override fun noAnno(person: Person): Flowable<Person> {
        TODO()
    }

    override fun emptyAnno(person: Person): Flowable<Person> {
        TODO()
    }
}

class DummyRSocket : AbstractRSocket()

data class Person(var name: String, var surname: String) {
    constructor() : this("", "")
}

data class PhoneNumber(var num: String) {
    constructor() : this("")
}


