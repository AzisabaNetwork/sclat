package net.azisaba.sclat.core.socket.equip

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class EquipmentServer(
    private val plugin: Plugin,
    private val port: Int,
) {
    private var serverSocket: ServerSocket? = null

    fun start() =
        plugin.launch {
            try {
                serverSocket = ServerSocket(port)
                println("Waiting for status client...")

                while (true) {
                    val clientSocket = serverSocket!!.accept()
                    plugin.launch { handleClient(clientSocket) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stop()
            }
        }

    private suspend fun handleClient(socket: Socket) =
        withContext(Dispatchers.IO) {
            println("Connected ${socket.remoteSocketAddress}")
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = PrintWriter(socket.getOutputStream(), true)

                println("Waiting for commands...")
                while (true) {
                    val cmd = reader.readLine() ?: break
                    if (cmd == "stop") {
                        println("Socket closed.")
                        break
                    }

                    // Todo: handle commands
//                EquipmentServerManager.addEquipmentCommand(cmd)

                    val args = cmd.split(" ")
                    when (args[0]) {
                        "setting" -> {
                            if (args.size == 3 && args[1].length == 9 && args[2].length == 36) {
//                            // Todo: handle setting command
//                                  Sclat.conf?.playerSettings?.set("Settings.${args[2]}", args[1])
                            }
                        }

                        "mod" -> {
                            // Todo: handle mod command
//                        if (args.size == 2 && !Sclat.modList.contains(args[1])) {
//                            Sclat.modList.add(args[1])
//                        }
                        }

                        "join" -> {
                            if (args.size == 2) {
                                // Handle join command
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                println("Disconnected ${socket.remoteSocketAddress}")
            }
        }

    fun stop() {
        try {
            serverSocket?.close()
            println("Equipment server is stopped!")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
