package be4rjp.sclat.server

import org.openjdk.jmh.annotations.*
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class EquipmentSocketBenchmark {
    private lateinit var server: EquipmentServer
    private val port = 12348

    @Setup(Level.Trial)
    fun setup() {
        server = EquipmentServer(port).apply { start() }
        Thread.sleep(1000) // Wait for server to start
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        server.interrupt()
    }

    @Benchmark
    fun singleClientBenchmark() {
        val client = Socket("localhost", port)
        val writer = PrintWriter(client.getOutputStream(), true)
        writer.println("setting 123456789 123e4567-e89b-12d3-a456-426614174000")
        Thread.sleep(500) // Allow server to process
        client.close()
    }

    @Benchmark
    fun multiClientBenchmark() {
        val clients =
            (1..10).map {
                thread {
                    val client = Socket("localhost", port)
                    val writer = PrintWriter(client.getOutputStream(), true)
                    writer.println("mod client$it")
                    Thread.sleep(500) // Allow server to process
                    client.close()
                }
            }

        clients.forEach { it.join() }
    }
}
