package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.contract.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

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

    fun noAnno(person: Person): Flowable<Person>

    @RequestStream
    fun emptyAnno(person: Person): Flowable<Person>

    @Close
    fun close(): Completable

    @OnClose
    fun onClose(): Completable
}

class PersonServiceHandler : PersonsService {
    override fun close(): Completable {
        return Completable.complete()
    }

    override fun onClose(): Completable {
        return Completable.complete()
    }

    override fun stream(person: Person): Flowable<Person> = Flowable.just(person.copy())

    override fun response(person: Person): Single<Person> = Single.just(person.copy())

    override fun fnf(person: Person): Completable = Completable.complete()

    override fun channel(person: Flowable<Person>)
            = Flowable.fromPublisher(person).map { it.copy() }

    override fun noAnno(person: Person): Flowable<Person> {
        TODO()
    }

    override fun emptyAnno(person: Person): Flowable<Person> {
        TODO()
    }

}

data class Person(var name: String, var surname: String) {
    constructor() : this("", "")
}

data class PhoneNumber(var num: String) {
    constructor() : this("")
}


