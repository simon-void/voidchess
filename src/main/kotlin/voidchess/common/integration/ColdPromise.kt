package voidchess.common.integration

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CancellationException


interface ColdPromise<out T> {
    fun computeAndCallback(callback: (Result<T>) -> Unit)
    fun cancel(cancellationMsg: String? = null)
}

class ColdPromiseImpl<out T>(
    private val generateValue: suspend () -> T
) : ColdPromise<T> {
    private var computeJob: Job? = null
    private var hasNotBeenCalled = true

    override fun computeAndCallback(
        callback: (Result<T>) -> Unit
    ) {
        check(hasNotBeenCalled) {"one-time function has already been called"}
        hasNotBeenCalled = false

        runBlocking {
            computeJob = launch {
                val result: Result<T> = try{
                    Result.success(generateValue())
                } catch (e: Throwable) {
                    Result.failure(e)
                }
                callback(result)
                computeJob = null
            }
        }
    }

    override fun cancel(cancellationMsg: String?) {
        computeJob?.cancel(cancellationMsg?.let { CancellationException(cancellationMsg) })
        computeJob = null
    }
}
