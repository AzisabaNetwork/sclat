package be4rjp.sclat.api.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AsyncTickThread {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun shutdown() {
        executor.shutdown()
    }

    fun runTask(runnable: Runnable) {
        executor.submit(runnable)
    }
}
