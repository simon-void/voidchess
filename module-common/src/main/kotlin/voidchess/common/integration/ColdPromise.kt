package voidchess.common.integration

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


interface ColdPromise<out T> {
    fun computeAndCallback(callback: (T) -> Unit)
    fun cancel()
}

class ColdPromiseImpl<out T>(
    private val generateValue: suspend () -> T
) : ColdPromise<T> {
    private var computeJob: Job? = null
    private var hasNotBeenCalled = true

    override fun computeAndCallback(
        callback: (T) -> Unit
    ) {
        check(hasNotBeenCalled) {"one-time function has already been called"}
        hasNotBeenCalled = false

        runBlocking {
            computeJob = launch {
                val value = generateValue()
                callback(value)
                computeJob = null
            }
        }
    }

    override fun cancel() {
        computeJob?.cancel()
        computeJob = null
    }
}
