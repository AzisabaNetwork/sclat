package be4rjp.sclat.server

import be4rjp.sclat.plugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class StatusClient(
    private val host: String?,
    private val port: Int,
    commands: MutableList<String?>,
) {
    private var commands: MutableList<String?> = ArrayList()

    private val task: BukkitRunnable

    init {
        this.commands = commands
        this.task =
            object : BukkitRunnable() {
                override fun run() {
                    var reader: BufferedReader? = null

                    try {
                        Socket(
                            host,
                            port,
                        ).use { cSocket ->
                            PrintWriter(cSocket.getOutputStream(), true).use { writer ->
                                try {
                                    // System.out.println("test");
                                    // IPアドレスとポート番号を指定してクライアント側のソケットを作成

                                    // クライアント側からサーバへの送信用

                                    // サーバ側からの受取用

                                    reader = BufferedReader(InputStreamReader(cSocket.getInputStream()))

                                    // 命令送信ループ
                                    var cmd: String? = null
                                    while (true) {
                                        if (!commands.isEmpty()) {
                                            cmd = commands.get(0)

                                            // 送信用の文字を送信
                                            writer.println(cmd)

                                            // stopの入力でループを抜ける
                                            if (cmd == "stop") {
                                                break
                                            }

                                            // サーバ側からの受取の結果を表示
                                            // System.out.println("result：" + reader.readLine());
                                            commands.removeAt(0)
                                        } else {
                                            break
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        println("Client is stopped!")
                    }
                }
            }
    }

    fun startClient() {
        this.task.runTaskAsynchronously(plugin)
    }

    fun addCommand(command: String?) {
        commands.add(command)
    }
}
