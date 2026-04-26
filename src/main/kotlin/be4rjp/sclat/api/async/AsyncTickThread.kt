package be4rjp.sclat.api.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Deprecated("Use mccoroutine")
class AsyncTickThread {
    private val executor: ExecutorService

    init {
        this.executor = Executors.newSingleThreadExecutor()
    }

    fun shutdown() {
        executor.shutdown()
    }

    fun runTask(runnable: Runnable) {
        executor.submit(runnable)
    }
}
