package be4rjp.sclat.commands;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.BungeeCordAPI;
import be4rjp.sclat.api.MessageType;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.SoundType;
import be4rjp.sclat.api.equipment.EquipmentClient;
import be4rjp.sclat.api.status.ServerStatus;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.emblem.EmblemManager;
import be4rjp.sclat.manager.ServerStatusManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static be4rjp.sclat.Sclat.conf;

@CommandAlias("sclat")
@CommandPermission("sclat.admin")
public class SclatCommand extends BaseCommand {
	private final Sclat plugin;
	public SclatCommand(Sclat plugin) {
		this.plugin = plugin;
	}

	@Subcommand("setUpdateRate")
	public void onSetUpdateRate(CommandSender sender, int rate) {
		conf.getConfig().set("BlockUpdateRate", rate);
		sender.sendMessage("setConfig [BlockUpdateRate]  :  " + rate);
	}

	@Subcommand("fly")
	@CommandCompletion("*")
	public void onFly(CommandSender sender, OnlinePlayer targetPlayer) {
		String playerName = targetPlayer.player.getName();
		Sclat.flyList.add(playerName);
		sender.sendMessage(String.format("%s was added to flyList", playerName));
	}

	@Subcommand("reloadEmblems")
	public void onReloadEmblems(CommandSender sender) {
		conf.loadEmblemLoreData();
		conf.loadEmblemUserData();
		sender.sendMessage("称号のデータを再読み込みしました。");
	}

	@Subcommand("migrateEmblems")
	public void onMigrateEmblems(CommandSender sender) {
		if (!conf.emblemsFile.exists()) {
			sender.sendMessage(ChatColor.RED + "Old emblem userdata file isn't exists.");
			return;
		}

		YamlConfiguration oldData = YamlConfiguration.loadConfiguration(conf.emblemsFile);
		Set<String> dataUuids = oldData.getKeys(false);
		for (String _uuid : dataUuids) {
			List<String> userEmblems = oldData.getStringList(_uuid);
			for (String emblem : userEmblems) {
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
	}

	@Subcommand("saveEmblems")
	public void onSaveEmblems(CommandSender sender) {
		try {
			conf.saveEmblemUserdata();
			sender.sendMessage(ChatColor.GREEN + "Successfully to save emblem userdata");
		} catch (IOException e) {
			sender.sendMessage(ChatColor.RED + "Failed to save emblem userdata");
			e.printStackTrace();
		}
	}

	@Subcommand("checkEmblems")
	public void onCheckEmblems(CommandSender sender) {
		Map<String, Map<String, Integer>> dataMap = EmblemManager.getDataMap();
		for (String _key : dataMap.keySet()) {
			sender.sendMessage(_key);
			Map<String, Integer> playerUuids = dataMap.getOrDefault(_key, new HashMap<>());
			playerUuids.forEach((k, v) -> sender.sendMessage("- " + k + ": " + v));
		}
	}

	@Subcommand("mod")
	public void onMod(CommandSender sender, String serverName) {
		if (sender instanceof Player) {
			for (ServerStatus ss : ServerStatusManager.serverList) {
				if (ss.getServerName().equals(serverName)) {
					List<String> commands = new ArrayList<>();
					commands.add("mod " + sender.getName());
					commands.add("stop");
					// Todo: use redis. fallbacks PluginMessaging
					EquipmentClient sc = new EquipmentClient(
							conf.getConfig().getString("EquipShare." + serverName + ".Host"),
							conf.getConfig().getInt("EquipShare." + serverName + ".Port"), commands);
					sc.startClient();

					SclatUtil.sendMessage("Moderatorとして転送中...", MessageType.PLAYER, (Player) sender);
					SclatUtil.sendMessage("2秒後に転送されます", MessageType.PLAYER, (Player) sender);
					SclatUtil.playGameSound((Player) sender, SoundType.SUCCESS);

					BukkitRunnable task = new BukkitRunnable() {
						@Override
						public void run() {
							try {
								BungeeCordAPI.PlayerSendServer((Player) sender, ss.getServerName());
								DataMgr.getPlayerData((Player) sender).setServerName(ss.getDisplayName());
							} catch (Exception ignored) {
							}
						}
					};
					task.runTaskLater(Sclat.getPlugin(), 40);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "This command is player only.");
		}
	}

	@Subcommand("ss")
	@CommandPermission("sclat.admin")
	public class SS extends BaseCommand {
		public void onMT(CommandSender sender, String server, boolean toMaintenance) {
			for (ServerStatus ss : ServerStatusManager.serverList) {
				if (ss.getServerName().equals(server)) {
					ss.setMaintenance(toMaintenance);
					sender.sendMessage("Switched " + ss.getDisplayName() + " §rto "
							+ (toMaintenance ? "§cMAINTENANCE" : "§6NORMAL"));
					return;
				}
			}
		}
	}

	@Subcommand("tutorial")
	public class Tutorial extends BaseCommand {
		@Subcommand("add")
		public void onAdd(CommandSender sender, String server) {
			List<String> list = Sclat.tutorialServers.getConfig().getStringList("server-list");
			if (!list.contains(server)) {
				list.add(server);
				Sclat.tutorialServers.getConfig().set("server-list", list);
			} else {
				sender.sendMessage(ChatColor.RED + "This server is already exist.");
			}
		}

		@Subcommand("list")
		public void onList(CommandSender sender) {
			List<String> list = Sclat.tutorialServers.getConfig().getStringList("server-list");
			sender.sendMessage(list.toString());
		}

		@Subcommand("reload")
		public void onReload(CommandSender sender) {
			Sclat.tutorialServers.reloadConfig();
			sender.sendMessage(ChatColor.GREEN + "Tutorial config was reloaded!");
		}
	}
}
