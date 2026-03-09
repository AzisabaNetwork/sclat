package be4rjp.sclat.server

import io.kotest.core.spec.style.StringSpec
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class EquipmentSocketIntegrationTest :
    StringSpec({

        "single client communication test" {
            val port = 12345
            val server = EquipmentServer(port).apply { start() }
            Thread.sleep(1000) // Wait for server to start

            val client = Socket("localhost", port)
            val writer = PrintWriter(client.getOutputStream(), true)
            writer.println("setting 123456789 123e4567-e89b-12d3-a456-426614174000")
            Thread.sleep(500) // Allow server to process

            // Verify the command was processed (mock EquipmentServerManager if needed)
            // Example: EquipmentServerManager.commands should contain the sent command

            client.close()
            server.interrupt()
        }

        "multi-client concurrency test" {
            val port = 12346
            val server = EquipmentServer(port).apply { start() }
            Thread.sleep(1000) // Wait for server to start

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
            server.interrupt()
        }

        "edge case: invalid command" {
            val port = 12347
            val server = EquipmentServer(port).apply { start() }
            Thread.sleep(1000) // Wait for server to start

            val client = Socket("localhost", port)
            val writer = PrintWriter(client.getOutputStream(), true)
            writer.println("invalid_command")
            Thread.sleep(500) // Allow server to process

            // Verify the server handled the invalid command gracefully
            // Example: No exceptions thrown, no invalid state changes

            client.close()
            server.interrupt()
        }
    })
