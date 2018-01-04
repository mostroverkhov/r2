package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.contract.RequestResponse
import com.github.mostroverkhov.r2.core.contract.Service
import com.github.mostroverkhov.r2.core.internal.responder.ResponderTargetResolver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.reactivestreams.Publisher

class ResponderTargetResolverTest {

    private lateinit var methodResolver: ResponderTargetResolver.MethodResolver
    @Before
    fun setUp() {
        methodResolver = ResponderTargetResolver.MethodResolver()
    }

    @Test
    fun resolveService() {
        val svc = ResponderTargetResolver.resolveSvcContract(TestHandler::class.java)
        assertTrue(svc == TestContract::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun resolveServiceNonContract() {
        val svc = ResponderTargetResolver.resolveSvcContract(NoSvcHandler::class.java)
        assertTrue(svc == TestContract::class.java)
    }

    @Test
    fun resolveMethod() {
        val svc = TestHandler()
        val method = methodResolver.resolve(svc, "response")
        assertNotNull(method)
        assertTrue(method.isAccessible)
    }

    @Test(expected = IllegalArgumentException::class)
    fun resolveMethodNonExistent() {
        val svc = TestHandler()
        methodResolver.resolve(svc, "resp")
    }

    @Test(expected = IllegalArgumentException::class)
    fun resolveMethodOverloaded() {
        val svc = OverloadHandler()
        methodResolver.resolve(svc, "response")
    }

    @Test
    fun resolveOneArg() {
        val svc = TestContractHandler()
        val targetMethod = methodResolver.resolve(svc, "response")
        val actionArgs = ResponderTargetResolver.resolveArguments(targetMethod)
        assertEquals(null, actionArgs.metadataPos())
        assertEquals(0, actionArgs.requestPos())
        assertEquals(Item::class.java, actionArgs.requestType())
    }

    @Test
    fun resolveTwoArgs() {
        val svc = TestContractHandler()
        val targetMethod = methodResolver.resolve(svc, "twoArgs")
        val actionArgs = ResponderTargetResolver.resolveArguments(targetMethod)
        assertEquals(0, actionArgs.requestPos())
        assertEquals(1, actionArgs.metadataPos())
        assertEquals(Item::class.java, actionArgs.requestType())
    }

    @Test
    fun resolveTwoArgsSwapped() {
        val svc = TestContractHandler()
        val targetMethod = methodResolver.resolve(svc, "twoArgsSwapped")
        val actionArgs = ResponderTargetResolver.resolveArguments(targetMethod)
        assertEquals(1, actionArgs.requestPos())
        assertEquals(0, actionArgs.metadataPos())
        assertEquals(Item::class.java, actionArgs.requestType())
    }

    @Test(expected = IllegalArgumentException::class)
    fun resolveMissingName() {
        val svc = TestContractHandler()
        methodResolver.resolve(svc, "missName")
    }
}

private class TestHandler : TestContract {
    override fun missName(metadata: Metadata, request: Item): Publisher<Item> {
        return Publisher { }
    }

    override fun twoArgsSwapped(metadata: Metadata, request: Item): Publisher<Item> {
        return Publisher { }
    }

    override fun twoArgs(request: Item, metadata: Metadata): Publisher<Item> {
        return Publisher { }
    }

    override fun response(request: Item): Publisher<Item> {
        return Publisher { }
    }
}

private class NoSvcHandler : NoSvcContract {

    override fun response(request: Item): Publisher<Item> {
        return Publisher { it.onComplete() }
    }
}

@Service("name")
private interface TestContract {
    @RequestResponse("response")
    fun response(request: Item): Publisher<Item>

    @RequestResponse("twoArgs")
    fun twoArgs(request: Item, metadata: Metadata): Publisher<Item>

    @RequestResponse("twoArgsSwapped")
    fun twoArgsSwapped(metadata: Metadata, request: Item): Publisher<Item>

    @RequestResponse
    fun missName(metadata: Metadata, request: Item): Publisher<Item>
}

private class TestContractHandler : TestContract {
    override fun response(request: Item): Publisher<Item> {
        return Publisher { }
    }

    override fun twoArgs(request: Item, metadata: Metadata): Publisher<Item> {
        return Publisher { }
    }

    override fun twoArgsSwapped(metadata: Metadata, request: Item): Publisher<Item> {
        return Publisher { }
    }

    override fun missName(metadata: Metadata, request: Item): Publisher<Item> {
        return Publisher { }
    }
}

private interface NoSvcContract {
    @RequestResponse
    fun response(request: Item): Publisher<Item>
}

private interface OverloadContract {
    @RequestResponse
    fun response(request: Item): Publisher<Item>

    @RequestResponse
    fun response(): Publisher<Item>
}

private class OverloadHandler : OverloadContract {

    override fun response(request: Item): Publisher<Item> = Publisher { }

    override fun response(): Publisher<Item> = Publisher { }
}


private data class Item(val name: String)