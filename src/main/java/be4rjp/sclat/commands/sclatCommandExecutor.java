package be4rjp.sclat.commands;

import be4rjp.sclat.Config;
import be4rjp.sclat.Main;
import be4rjp.sclat.MessageType;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.ServerType;
import be4rjp.sclat.SoundType;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.ServerStatus;
import be4rjp.sclat.emblem.EmblemManager;
import be4rjp.sclat.manager.BungeeCordMgr;
import be4rjp.sclat.manager.ServerStatusManager;
import be4rjp.sclat.server.EquipmentClient;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static be4rjp.sclat.Main.conf;

//sclat Command
public class sclatCommandExecutor implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (args == null) return false;
        if (args.length == 0) return false;

        //------------------------Check sender type-----------------------------
        CommanderType type = CommanderType.CONSOLE;
        if (sender instanceof Player) {
            if (sender.hasPermission("sclat.admin"))
                type = CommanderType.ADMIN;
            else
                type = CommanderType.MEMBER;
        }
        //----------------------------------------------------------------------

        //-------------------------/sclat setUpdateRate----------------------------
        if (args[0].equalsIgnoreCase("setUpdateRate") || args[0].equalsIgnoreCase("sur")) {
            if (args.length != 2) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            String num = args[1];
            if (Sclat.isNumber(num)) {
                Main.conf.getConfig().set("BlockUpdateRate", Integer.valueOf(num));
                sender.sendMessage("setConfig [BlockUpdateRate]  :  " + num);
                return true;
            } else {
                sender.sendMessage("Please type with number");
                return false;
            }
        }
        //-------------------------------------------------------------------------

        //----------------------------/sclat fly-----------------------------------
        if (args[0].equalsIgnoreCase("fly")) {
            if (args.length != 2) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            String playerName = args[1];
            for (Player player : Main.getPlugin().getServer().getOnlinePlayers()) {
                if (playerName.equals(player.getName())) {
                    Main.flyList.add(playerName);
                    return true;
                }
            }
        }
        //-------------------------------------------------------------------------


        //----- /sclat refresh-config-----
        if (args[0].equalsIgnoreCase("refresh-config")) {
            if (args.length != 2) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                String targetConfig = args[1];
                Sclat.sendMessage(String.format("%sの設定を再読み込み中...", targetConfig), MessageType.PLAYER, player);
                switch (targetConfig.toLowerCase()) {
                    case "emblemuserdata":
                        conf.loadEmblemUserData();
                        break;
                    case "emblemloredata":
                        conf.loadEmblemLoreData();
                        break;
                    default:
                        Sclat.sendMessage("そのオプションは存在しません！", MessageType.PLAYER, player);
                        return true;
                }
                Sclat.sendMessage("再読み込み完了", MessageType.PLAYER, player);
            }
        }
        //--------------------------------

        // ----------------/sclat migrate-emblems-----------------
        if(args[0].equalsIgnoreCase("migrate-emblems")) {
            if(args.length != 1) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            if(!conf.emblemsFile.exists()) {
                sender.sendMessage(ChatColor.RED + "Old emblem userdata file isn't exists.");
                return true;
            }

            YamlConfiguration oldData = YamlConfiguration.loadConfiguration(conf.emblemsFile);
            Set<String> dataUuids = oldData.getKeys(false);
            for (String _uuid : dataUuids) {
                List<String> userEmblems = oldData.getStringList(_uuid);
                for(String emblem: userEmblems) {
                    conf.getEmblemUserdata().set(_uuid + "." + emblem, 1);
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Migration was succeeded!");

            try {
                conf.saveEmblemUserdata();
                sender.sendMessage(ChatColor.GREEN + "Successfully to save emblem userdata");
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed to save emblem userdata");
                e.printStackTrace();
            }
            return true;
        }
        // -------------------------------------------------------

        // ----------------/sclat save-emblems--------------------
        if(args[0].equalsIgnoreCase("save-emblems")) {
            if (args.length != 1) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            try {
                conf.saveEmblemUserdata();
                sender.sendMessage(ChatColor.GREEN + "Successfully to save emblem userdata");
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Failed to save emblem userdata");
                e.printStackTrace();
            }
            return true;
        }
        // -------------------------------------------------------

        // ----------------/sclat check-emblems-------------------
        if(args[0].equalsIgnoreCase("check-emblems")) {
            if (args.length != 1) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            Map<String, Map<String, Integer>> dataMap = EmblemManager.getDataMap();
            for(String _key: dataMap.keySet()) {
                sender.sendMessage(_key);
                Map<String, Integer> playerUuids = dataMap.getOrDefault(_key, new HashMap<>());
                playerUuids.forEach((k,v) -> sender.sendMessage("- " + k + ": " + v));
            }
        }
        // --------------------------------------------------------

        //----------------------------/sclat mod-----------------------------------
        if (args[0].equalsIgnoreCase("mod")) {
            if (args.length != 2) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            if (sender instanceof Player) {
                String serverName = args[1];
                for (ServerStatus ss : ServerStatusManager.serverList) {
                    if (ss.getServerName().equals(serverName)) {
                        List<String> commands = new ArrayList<>();
                        commands.add("mod " + sender.getName());
                        commands.add("stop");
                        EquipmentClient sc = new EquipmentClient(conf.getConfig().getString("EquipShare." + serverName + ".Host"),
                                conf.getConfig().getInt("EquipShare." + serverName + ".Port"), commands);
                        sc.startClient();

                        Sclat.sendMessage("Moderatorとして転送中...", MessageType.PLAYER, (Player) sender);
                        Sclat.sendMessage("2秒後に転送されます", MessageType.PLAYER, (Player) sender);
                        Sclat.playGameSound((Player) sender, SoundType.SUCCESS);

                        BukkitRunnable task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                try {
                                    BungeeCordMgr.PlayerSendServer((Player) sender, ss.getServerName());
                                    DataMgr.getPlayerData((Player) sender).setServerName(ss.getDisplayName());
                                } catch (Exception ignored) {
                                }
                            }
                        };
                        task.runTaskLater(Main.getPlugin(), 40);
                    }
                }
                return true;
            } else {
                return false;
            }

        }
        //-------------------------------------------------------------------------


        //------------------/sclat ss <status> <server> <flag>---------------------
        if (args[0].equalsIgnoreCase("ss")) {
            if (args.length < 4 || Main.type != ServerType.LOBBY) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            if (args[1].equals("mt")) {
                String server = args[2];
                for (ServerStatus ss : ServerStatusManager.serverList) {
                    if (ss.getServerName().equals(server)) {
                        ss.setMaintenance(args[3].equals("true"));
                        sender.sendMessage("Switched " + ss.getDisplayName() + " §rto " + (args[3].equals("true") ? "§cMAINTENANCE" : "§6NORMAL"));
                        return true;
                    }
                }
            }
        }
        //-------------------------------------------------------------------------

        //---------------------/sclat tutorial <option> <server>-------------------
        if (args[0].equalsIgnoreCase("tutorial")) {
            if (args.length < 2 || Main.type != ServerType.LOBBY) return false;

            if (type == CommanderType.MEMBER) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                Sclat.playGameSound((Player) sender, SoundType.ERROR);
                return true;
            }

            if (args[1].equals("add")) {
                if (args.length < 3) return false;
                String server = args[2];
                List<String> list = Main.tutorialServers.getConfig().getStringList("server-list");
                if (!list.contains(server)) {
                    list.add(server);
                    Main.tutorialServers.getConfig().set("server-list", list);
                } else {
                    sender.sendMessage(ChatColor.RED + "This server is already exist.");
                }
                return true;
            } else if (args[1].equals("list")) {
                List<String> list = Main.tutorialServers.getConfig().getStringList("server-list");
                sender.sendMessage(list.toString());
                return true;
            } else if (args[1].equals("reload")) {
                Main.tutorialServers.reloadConfig();
                return true;
            }
        }
        //-------------------------------------------------------------------------
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        //------------------------Check sender type-----------------------------
        CommanderType type = CommanderType.CONSOLE;
        if (sender instanceof Player) {
            if (sender.hasPermission("sclat.admin"))
                type = CommanderType.ADMIN;
            else
                type = CommanderType.MEMBER;
        }
        //----------------------------------------------------------------------

        //-----------------------------Tab complete-----------------------------
        if (args.length == 1) {
            List<String> list = new ArrayList<>();

            list.add("help");

            if (type != CommanderType.MEMBER) {
                list.add("setUpdateRate");
                list.add("sur");
                list.add("fly");
                list.add("ss");
                list.add("tutorial");
                list.add("refresh-config");
                list.add("migrate-emblems");
                list.add("save-emblems");
                list.add("check-emblems");
            }

            return list;

        } else if (args.length == 2) {
            List<String> list = new ArrayList<>();
            if (type != CommanderType.MEMBER) {
                if (args[1].equalsIgnoreCase("refresh-config")) {
                    list.add("emblemuserdata");
                    list.add("emblemloredata");
                }
            }
            return list;
        }
        return null;
        //----------------------------------------------------------------------
    }
}
