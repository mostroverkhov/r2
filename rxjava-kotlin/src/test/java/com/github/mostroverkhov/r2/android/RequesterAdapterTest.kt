package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.RequesterAdapter
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subscribers.TestSubscriber
import io.rsocket.android.AbstractRSocket
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RequesterAdapterTest {
    lateinit var adapter: CallAdapter
    @Before
    fun setUp() {
        adapter = RequesterAdapter(object : AbstractRSocket() {})
    }


    @Test(expected = RuntimeException::class)
    fun wrongReturnType() {
        val obj = Stub::class.java.getMethod("obj")
        adapter.resolve(obj, RuntimeException())
    }

    @Test
    fun singleType() {
        assertError<Single<Any>>("single", { it.toFlowable() })
    }

    @Test
    fun flowableType() {
        assertError<Flowable<Any>>("flowable", { it })
    }

    @Test
    fun completableType() {
        assertError<Completable>("compl", { it.toFlowable() })
    }

    private inline fun <reified T> assertError(method: String,
                                               toFlowable: (T) -> Flowable<Any>) {

        val obj = Stub::class.java.getMethod(method)
        val ex = RuntimeException()
        val resolve = adapter.resolve(obj, ex)
        assertTrue(resolve is T )
        val compl = toFlowable(resolve as T)
        val subscriber = TestSubscriber<Any>()
        compl.blockingSubscribe(subscriber)
        subscriber.assertNoValues()
        subscriber.assertError(ex.javaClass)
    }

    private interface Stub {
        fun obj(): Any

        fun single(): Single<String>

        fun flowable(): Flowable<String>

        fun compl(): Completable
    }
}