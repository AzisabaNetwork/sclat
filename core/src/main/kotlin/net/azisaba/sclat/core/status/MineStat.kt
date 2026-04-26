package net.azisaba.sclat.core.status

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

/**
 *
 * from [MineStat.java](https://github.com/ldilley/minestat/blob/master/Java/me/dilley/MineStat.java)
 */
class MineStat
    @JvmOverloads
    constructor(
        address: String?,
        port: Int,
        timeout: Int = DEFAULT_TIMEOUT,
    ) {
        /**
         * Hostname or IP address of the Minecraft server
         */
        var address: String? = null

        /**
         * Port number the Minecraft server accepts connections on
         */
        var port: Int = 0

        /**
         * TCP socket connection timeout in milliseconds
         */
        private var timeout = 0

        /**
         * Is the server up? (true or false)
         */
        var isServerUp: Boolean = false
            private set

        /**
         * Message of the day from the server
         */
        var motd: String? = null

        /**
         * Minecraft version the server is running
         */
        var version: String? = null

        /**
         * Current number of players on the server
         */
        @JvmField
        var currentPlayers: String? = null

        /**
         * Maximum player capacity of the server
         */
        var maximumPlayers: String? = null

        /**
         * Ping time to server in milliseconds
         */
        var latency: Long = 0

        init {
            this.address = address
            this.port = port
            setTimeout(timeout)
            refresh()
        }

        /**
         * Refresh state of the server
         *
         * @return `true`; `false` if the server is down
         */
        fun refresh(): Boolean {
            val serverData: Array<String?>?
            val rawServerData: String?
            try {
                // Socket clientSocket = new Socket(getAddress(), getPort());
                val clientSocket = Socket()
                val startTime = System.currentTimeMillis()
                clientSocket.connect(InetSocketAddress(this.address, this.port), timeout)
                this.latency = System.currentTimeMillis() - startTime
                val dos = DataOutputStream(clientSocket.getOutputStream())
                val br = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val payload = byteArrayOf(0xFE.toByte(), 0x01.toByte())
                // dos.writeBytes("\u00FE\u0001");
                dos.write(payload, 0, payload.size)
                rawServerData = br.readLine()
                clientSocket.close()
            } catch (e: Exception) {
                this.isServerUp = false
                // e.printStackTrace();
                return this.isServerUp
            }

            if (rawServerData == null) {
                this.isServerUp = false
            } else {
                serverData =
                    rawServerData.split("\u0000\u0000\u0000".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (serverData.size >= NUM_FIELDS) {
                    this.isServerUp = true
                    this.version = serverData[2]!!.replace("\u0000", "")
                    this.motd = serverData[3]!!.replace("\u0000", "")
                    this.currentPlayers = serverData[4]!!.replace("\u0000", "")
                    this.maximumPlayers = serverData[5]!!.replace("\u0000", "")
                } else {
                    this.isServerUp = false
                }
            }
            return this.isServerUp
        }

        fun getTimeout(): Int {
            return timeout * 1000 // milliseconds
        }

        fun setTimeout(timeout: Int) {
            this.timeout = timeout * 1000 // milliseconds
        }

        companion object {
            const val NUM_FIELDS: Byte = 6 // expected number of fields returned from server after query
            const val DEFAULT_TIMEOUT: Int = 5 // default TCP socket connection timeout in seconds
        }
    }
