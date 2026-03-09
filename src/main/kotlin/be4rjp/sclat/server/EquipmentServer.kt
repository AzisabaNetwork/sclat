package be4rjp.sclat.server

import be4rjp.sclat.Sclat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class EquipmentServer(
    private val port: Int,
) : Thread() {
    override fun run() {
        try {
            ServerSocket(port).use { serverSocket ->
                println("Waiting for status client...")

                while (true) {
                    val socket = serverSocket.accept()
                    EquipEchoThread(socket).start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            println("Equipment server is stopped!")
        }
    }
}

internal class EquipEchoThread(
    private val socket: Socket,
) : Thread() {
    init {
        println("Connected ${socket.remoteSocketAddress}")
    }

    override fun run() {
        socket.use {
            try {
                println("Waiting for commands...")
                val reader = BufferedReader(InputStreamReader(it.getInputStream()))

                while (true) {
                    val cmd = reader.readLine() ?: break

                    if (cmd == "stop") {
                        println("Socket closed.")
                        break
                    }

                    EquipmentServerManager.addEquipmentCommand(cmd)
                    handleCommand(cmd)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                println("Disconnected ${socket.remoteSocketAddress}")
            }
        }
    }

    private fun handleCommand(cmd: String) {
        val args = cmd.split(" ")
        when (args[0]) {
            "setting" -> handleSettingCommand(args)
            "mod" -> handleModCommand(args)
            "join" -> handleJoinCommand(args)
        }
    }

    private fun handleSettingCommand(args: List<String>) {
        if (args.size == 3 && args[1].length == 9 && args[2].length == 36) {
            Sclat.conf?.playerSettings?.set("Settings.${args[2]}", args[1])
        }
    }

    private fun handleModCommand(args: List<String>) {
        if (args.size == 2 && !Sclat.modList.contains(args[1])) {
            Sclat.modList.add(args[1])
        }
    }

    private fun handleJoinCommand(args: List<String>) {
        if (args.size == 2) {
            println("Join command received with argument: ${args[1]}")
        }
    }
}
