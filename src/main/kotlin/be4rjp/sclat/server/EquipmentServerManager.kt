package be4rjp.sclat.server

import be4rjp.sclat.api.SclatUtil
import be4rjp.sclat.data.DataMgr.getPlayerData
import be4rjp.sclat.data.DataMgr.getWeaponClass
import be4rjp.sclat.manager.PlayerStatusMgr
import be4rjp.sclat.plugin
import be4rjp.sclat.sclatLogger

object EquipmentServerManager {
    var commands: MutableList<String> = ArrayList<String>()

    fun addEquipmentCommand(command: String) {
        sclatLogger.debug("Equip command: $command")
        commands.add(command)
    }

    @JvmStatic
    fun doCommands() {
        for (cmd in commands) {
            val args: Array<String?> = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            when (args[0]) {
                "set" -> {
                    // add [statusName] [number or name] [uuid]
                    if (args.size == 4) {
                        if (args[3]!!.length == 36) {
                            when (args[1]) {
                                "weapon" -> for (player in plugin
                                    .server
                                    .onlinePlayers) {
                                    if (player
                                            .uniqueId
                                            .toString() == args[3]
                                    ) {
                                        getPlayerData(player)!!.weaponClass = getWeaponClass(args[2])
                                    }
                                }

                                "gear" -> for (player in plugin
                                    .server
                                    .onlinePlayers) {
                                    if (player.uniqueId.toString() == args[3] &&
                                        SclatUtil.isNumber(args[2]!!)
                                    ) {
                                        getPlayerData(player)!!.gearNumber = args[2]!!.toInt()
                                    }
                                }

                                "rank" -> PlayerStatusMgr.setRank(args[3], args[2]!!.toInt())

                                "lv" -> PlayerStatusMgr.setLv(args[3], args[2]!!.toInt())
                            }
                        }
                    }
                }
            }
        }
        commands.clear()
    }
}
