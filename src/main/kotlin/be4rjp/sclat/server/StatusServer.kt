package be4rjp.sclat.server

import be4rjp.sclat.api.MessageType
import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.api.SclatUtil.sendMessage
import be4rjp.sclat.api.SoundType
import be4rjp.sclat.manager.PlayerReturnManager
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.manager.RankMgr
import be4rjp.sclat.manager.ServerStatusManager
import be4rjp.sclat.plugin
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class StatusServer( // private List<String> commands = new ArrayList<>();
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
                EchoThread(socket).start()

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
                println("Status server is stopped!")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

// 非同期スレッド
internal class EchoThread(
    private val socket: Socket,
) : Thread() {
    init {
        println("Connected " + socket.getRemoteSocketAddress())
    }

    override fun run() {
        try {
            println("Waiting for commands...")
            // クライアントからの受取用
            val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

            // サーバーからクライアントへの送信用
            PrintWriter(socket.getOutputStream(), true)

            var cmd: String? = null
            // 命令受け取り用ループ
            while (true) {
                if ((reader.readLine().also { cmd = it }) != null) {
                    if (cmd == "stop") {
                        socket.close()
                        println("Socket closed.")
                        break
                    }

                    println(cmd)

                    val args: Array<String?> = cmd!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (args[0] == "return" && args.size == 2) {
                        if (args[1]!!.length == 36) {
                            PlayerReturnManager.addPlayerReturn(args[1])
                        }
                    }

                    when (args[0]) {
                        "get" -> {
                            if (args.size == 3) {
                            }
                        }

                        "add" -> {
                            // add [statusName] [number] [uuid]
                            if (args.size == 4) {
                                if (SclatUtil.isNumber(args[2]!!) && args[3]!!.length == 36) {
                                    when (args[1]) {
                                        "money" -> PlayerStatusMgr.addMoney(args[3], args[2]!!.toInt())
                                        "rank" -> RankMgr.addPlayerRankPoint(args[3], args[2]!!.toInt())
                                        "level" -> PlayerStatusMgr.addLv(args[3], args[2]!!.toInt())
                                        "kill" -> PlayerStatusMgr.addKill(args[3], args[2]!!.toInt())
                                        "paint" -> PlayerStatusMgr.addPaint(args[3], args[2]!!.toInt())
                                        "ticket" -> PlayerStatusMgr.addTicketUuid(args[3], args[2]!!.toInt())
                                    }
                                }
                            }
                        }

                        "started" -> {
                            if (args.size == 3) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.runningMatch = true
                                        ss.waitingEndTime = 0
                                        ss.matchStartTime = args[2]!!.toLong()
                                    }
                                }
                            }
                        }

                        "cd" -> {
                            if (args.size == 3) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.waitingEndTime = args[2]!!.toLong()
                                        sendMessage(
                                            ss.displayName + "§aの試合待機が開始されました！",
                                            MessageType.ALL_PLAYER,
                                        )
                                        sendMessage(
                                            (
                                                "§a§l" + (ss.waitingEndTime - (System.currentTimeMillis() / 1000)) +
                                                    "§b秒後に開始されます"
                                            ),
                                            MessageType.ALL_PLAYER,
                                        )
                                        plugin
                                            .server
                                            .onlinePlayers
                                            .forEach { player: Player? ->
                                                SclatUtil.playGameSound(
                                                    player!!,
                                                    SoundType.SUCCESS,
                                                )
                                            }
                                    }
                                }
                            }
                        }

                        "cdc" -> {
                            if (args.size == 2) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.waitingEndTime = 0
                                    }
                                }
                            }
                        }

                        "stopped" -> {
                            if (args.size == 2) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.runningMatch = false
                                        ss.matchStartTime = 0
                                    }
                                }
                            }
                        }

                        "restart" -> {
                            if (args.size == 2) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.restartingServer = true
                                    }
                                }
                            }
                        }

                        "restarted" -> {
                            if (args.size == 2) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.restartingServer = false
                                    }
                                }
                            }
                        }

                        "map" -> {
                            if (args.size == 3) {
                                for (ss in ServerStatusManager.serverList) {
                                    if (ss.serverName == args[1]) {
                                        ss.mapName = args[2]
                                    }
                                }
                            }
                        }

                        "tutorial" -> {
                            if (args.size == 2) {
                                PlayerStatusMgr.setTutorialState(args[1], 1)
                            }
                        }
                    }
                } else {
                    break
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                socket?.close()
            } catch (e: IOException) {
            }
            println("Disconnected " + socket!!.getRemoteSocketAddress())
        }
    }
}
