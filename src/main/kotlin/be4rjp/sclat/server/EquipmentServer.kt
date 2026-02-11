package be4rjp.sclat.server

import be4rjp.sclat.Sclat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class EquipmentServer( // private List<String> commands = new ArrayList<>();
    private val port: Int,
) : Thread() {
    private var sSocket: ServerSocket? = null

    override fun run() {
        try {
            // ソケットを作成
            sSocket = ServerSocket(port)
            println("Waiting for status client...")

            // クライアントからの要求待ち
            while (true) {
                val socket = sSocket!!.accept()
                EquipEchoThread(socket).start()
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (sSocket != null) sSocket!!.close()
                println("Equipment server is stopped!")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

// 非同期スレッド
internal class EquipEchoThread(
    private val socket: Socket,
) : Thread() {
    init {
        println("Connected " + socket.getRemoteSocketAddress())
    }

    override fun run() {
        try {
            println("Waiting for commands...")
            // クライアントからの受取用
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            // サーバーからクライアントへの送信用
            PrintWriter(socket.getOutputStream(), true)

            var cmd: String
            // 命令受け取り用ループ
            while (true) {
                cmd = reader.readLine()
                if (cmd == "stop") {
                    socket.close()
                    println("Socket closed.")
                    break
                }

                EquipmentServerManager.addEquipmentCommand(cmd)

                val args: Array<String?> = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                when (args[0]) {
                    "setting" -> {
                        // setting [settingData] [uuid]
                        if (args.size == 3) {
                            if (args[1]!!.length == 9 && args[2]!!.length == 36) {
                                Sclat.conf?.playerSettings!!.set("Settings." + args[2], args[1])
                            }
                        }
                    }

                    "mod" -> {
                        // mod [PlayerName]
                        if (args.size == 2) {
                            if (Sclat.modList.contains(args[1])) {
                                Sclat.modList.add(args[1])
                            }
                        }
                    }

                    "join" -> {
                        if (args.size == 2) {
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
            }
            println("Disconnected " + socket.getRemoteSocketAddress())
        }
    }
}
