package net.azisaba.sclat.core.socket.equip

import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class EquipmentClient(
    private val plugin: Plugin,
    private val host: String,
    private val port: Int,
) {
    fun send(commands: List<String>) {
        plugin.launch {
            try {
                Socket(host, port).use { socket ->
                    PrintWriter(socket.getOutputStream(), true).use { writer ->
                        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
                            for (command in commands) {
                                writer.println(command)

                                // Optionally, handle server response
//                                val response = reader.readLine()
//                                println("Server response: $response")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
