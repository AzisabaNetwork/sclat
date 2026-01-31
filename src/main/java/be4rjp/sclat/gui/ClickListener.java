
package be4rjp.sclat.gui;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.VariablesKt;
import be4rjp.sclat.api.MessageType;
import be4rjp.sclat.api.SclatUtil;
import be4rjp.sclat.api.ServerType;
import be4rjp.sclat.api.SoundType;
import be4rjp.sclat.data.BlockUpdater;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.Match;
import be4rjp.sclat.data.PaintData;
import be4rjp.sclat.data.ServerStatus;
import be4rjp.sclat.data.WeaponClass;
import be4rjp.sclat.manager.ArmorStandMgr;
import be4rjp.sclat.manager.BungeeCordMgr;
import be4rjp.sclat.manager.MatchMgr;
import be4rjp.sclat.manager.PlayerStatusMgr;
import be4rjp.sclat.manager.SPWeaponMgr;
import be4rjp.sclat.manager.ServerStatusManager;
import be4rjp.sclat.manager.SquidMgr;
import be4rjp.sclat.manager.SuperJumpMgr;
import be4rjp.sclat.manager.WeaponClassMgr;
import be4rjp.sclat.tutorial.Tutorial;
import be4rjp.sclat.weapon.Brush;
import be4rjp.sclat.weapon.Bucket;
import be4rjp.sclat.weapon.Buckler;
import be4rjp.sclat.weapon.Charger;
import be4rjp.sclat.weapon.Decoy;
import be4rjp.sclat.weapon.Funnel;
import be4rjp.sclat.weapon.Gear;
import be4rjp.sclat.weapon.Hound;
import be4rjp.sclat.weapon.Kasa;
import be4rjp.sclat.weapon.Manuber;
import be4rjp.sclat.weapon.Reeler;
import be4rjp.sclat.weapon.Roller;
import be4rjp.sclat.weapon.Shooter;
import be4rjp.sclat.weapon.Spinner;
import be4rjp.sclat.weapon.Swapper;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import static be4rjp.sclat.Sclat.conf;

/**
 *
 * @author Be4rJP
 */
public class ClickListener implements Listener {
	@EventHandler
	public void onGUIClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null
				|| event.getCurrentItem().getItemMeta().getDisplayName() == null || event.getView().getTitle() == null)
			return;

		String name = event.getCurrentItem().getItemMeta().getDisplayName();
		Player player = (Player) event.getWhoClicked();

		if (name.equals(".")) {
			event.setCancelled(true);
			return;
		}

		if (name.isEmpty())
			return;
		else
			player.closeInventory();
		// player.sendMessage(name);

		switch (name) {
			case "試合に参加 / JOIN THE MATCH" :
				if (Sclat.type == ServerType.LOBBY)
					ServerStatusManager.openServerList(player);
				else
					MatchMgr.PlayerJoinMatch(player);
				break;
			case "装備変更 / EQUIPMENT" :
				OpenGUI.equipmentGUI(player, false);
				break;
			case "§bギア変更 / GEAR" :
				OpenGUI.gearGUI(player, false);
				break;
			case "§6武器変更 / WEAPON" :
				OpenGUI.openWeaponSelect(player, "Main", "null", false);
				break;
			case "§bギア購入 / GEAR" :
				OpenGUI.gearGUI(player, true);
				break;
			case "§6武器購入 / WEAPON" :
				OpenGUI.openWeaponSelect(player, "Main", "null", true);
				break;
			case "設定 / SETTINGS" :
				OpenGUI.openSettingsUI(player);
				break;
			case "ショップを開く / OPEN SHOP" :
				OpenGUI.equipmentGUI(player, true);
				break;
			case "塗りをリセット / RESET INK" :
				if (MatchMgr.canRollback) {
					SclatUtil.sendMessage("§a§lインクがリセットされました！", MessageType.ALL_PLAYER);
					SclatUtil.sendMessage("§a§l3分後に再リセットできるようになります", MessageType.ALL_PLAYER);
					for (Player op : VariablesKt.getPlugin().getServer().getOnlinePlayers())
						SclatUtil.playGameSound(op, SoundType.SUCCESS);
				}
				Match match = DataMgr.getPlayerData(player).match;
				match.getBlockUpdater().stop();
				MatchMgr.RollBack();
				player.setExp(0.99F);
				BlockUpdater bur = new BlockUpdater();
				if (conf.config.contains("BlockUpdateRate"))
					bur.setMaxBlockInOneTick(conf.config.getInt("BlockUpdateRate"));
				bur.start();
				match.setBlockUpdater(bur);
				List<Block> blocks = new ArrayList<>();
				Block b0 = Sclat.lobby.getBlock().getRelative(BlockFace.DOWN);
				blocks.add(b0);
				blocks.add(b0.getRelative(BlockFace.EAST));
				blocks.add(b0.getRelative(BlockFace.NORTH));
				blocks.add(b0.getRelative(BlockFace.SOUTH));
				blocks.add(b0.getRelative(BlockFace.WEST));
				blocks.add(b0.getRelative(BlockFace.NORTH_EAST));
				blocks.add(b0.getRelative(BlockFace.NORTH_WEST));
				blocks.add(b0.getRelative(BlockFace.SOUTH_EAST));
				blocks.add(b0.getRelative(BlockFace.SOUTH_WEST));
				for (Block block : blocks) {
					if (block.getType().equals(Material.WHITE_STAINED_GLASS)) {
						PaintData pdata = new PaintData(block);
						pdata.match = (match);
						pdata.team = (match.team0);
						pdata.setOrigianlType(block.getType());
						DataMgr.setPaintDataFromBlock(block, pdata);
						block.setType(match.team0.getTeamColor().glass);
					}
				}
				break;
			case "ロビーへ戻る / RETURN TO LOBBY" :
				if (Sclat.type != ServerType.LOBBY) {
					BungeeCordMgr.PlayerSendServer(player, "sclat");
					DataMgr.getPlayerData(player).setServerName("Sclat");
				} else {
					BungeeCordMgr.PlayerSendServer(player, "lobby");
					DataMgr.getPlayerData(player).setServerName("Lobby");
				}
				break;
			case "称号 / EMBLEM" :
				OpenGUI.openEmblemMenu(player);
				break;
			case "試し打ちサーバーへ接続 / TRAINING FIELD" :
				BungeeCordMgr.PlayerSendServer(player, "sclattest");
				DataMgr.getPlayerData(player).setServerName("sclattest");
				break;
			case "チームデスマッチサーバーへ接続 / CONNECT TO TDM SERVER" :
				BungeeCordMgr.PlayerSendServer(player, "tdm");
				DataMgr.getPlayerData(player).setServerName("TDM");
				break;
			case "ナワバリバトル" :
				Match ma = DataMgr.getMatchFromId(MatchMgr.matchcount);
				ma.addnawabariTCount();
				break;
			case "チームデスマッチ" :
				Match m = DataMgr.getMatchFromId(MatchMgr.matchcount);
				m.addtdmTCount();
				break;
			case "ガチエリア" :
				Match m2 = DataMgr.getMatchFromId(MatchMgr.matchcount);
				m2.addgatiareaTCount();
				break;
			case "戻る" :
				if (!name.equals("武器選択") || !name.equals("Shop"))
					OpenGUI.openMenu(player);
				break;
		}
		if (name.equals("リソースパックをダウンロード / DOWNLOAD RESOURCEPACK"))
			player.setResourcePack(conf.config.getString("ResourcePackURL"));
		if (event.getView().getTitle().equals("Gear")) {
			for (int i = 0; i <= 9;) {
				if (Gear.getGearName(i).equals(name)) {
					DataMgr.getPlayerData(player).gearNumber = i;
					PlayerStatusMgr.setGear(player, i);
					SclatUtil.sendMessage("ギア[" + ChatColor.AQUA + name + ChatColor.RESET + "]を選択しました",
							MessageType.PLAYER, player);
					break;
				}
				i++;
			}
		} else if (event.getView().getTitle().equals("Gear shop")) {
			for (int i = 0; i <= 9;) {
				if (Gear.getGearName(i).equals(name)) {
					if (PlayerStatusMgr.getMoney(player) >= Gear.getGearPrice(i)) {
						PlayerStatusMgr.addGear(player, i);
						PlayerStatusMgr.subMoney(player, Gear.getGearPrice(i));
						SclatUtil.sendMessage(ChatColor.GREEN + "購入に成功しました", MessageType.PLAYER, player);
						SclatUtil.playGameSound(player, SoundType.SUCCESS);
						PlayerStatusMgr.sendHologramUpdate(player);
					} else {
						SclatUtil.sendMessage(ChatColor.RED + "お金が足りません", MessageType.PLAYER, player);
						SclatUtil.playGameSound(player, SoundType.ERROR);
					}
					break;
				}
				i++;
			}
		}
		if (event.getView().getTitle().equals("Server List")) {
			for (ServerStatus ss : ServerStatusManager.serverList) {
				if (ss.displayName.equals(name)) {
					if (ss.getRestartingServer()) {
						SclatUtil.sendMessage("§c§nこのサーバーは再起動中です1~2分程度お待ちください", MessageType.PLAYER, player);
						SclatUtil.playGameSound(player, SoundType.ERROR);
						return;
					}
					if (ss.isOnline()) {
						if (ss.getPlayerCount() < ss.maxPlayer) {
							if (ss.getRunningMatch()) {
								SclatUtil.sendMessage("§c§nこのサーバーは試合中のため参加できません", MessageType.PLAYER, player);
								SclatUtil.playGameSound(player, SoundType.ERROR);
								return;
							}
							BungeeCordMgr.PlayerSendServer(player, ss.serverName);
							DataMgr.getPlayerData(player).setServerName(ss.displayName);
						} else {
							SclatUtil.sendMessage("§c§nこのサーバーは満員のため参加できません", MessageType.PLAYER, player);
							SclatUtil.playGameSound(player, SoundType.ERROR);
						}
					} else {
						if (ss.isMaintenance())
							SclatUtil.sendMessage("§c§nこのサーバーは現在メンテナンス中のため参加できません", MessageType.PLAYER, player);
						else
							SclatUtil.sendMessage("§c§nこのサーバーは現在再起動中です1~2分程度お待ちください。", MessageType.PLAYER, player);
						SclatUtil.playGameSound(player, SoundType.ERROR);
					}
					return;
				}
			}
		}

		if (event.getView().getTitle().equals("武器選択")) {
			if (name.equals("装備選択へ戻る") || name.equals("戻る") || name.equals("シューター") || name.equals("ローラー")
					|| name.equals("チャージャー") || name.equals("ブラスター") || name.equals("バーストシューター")
					|| name.equals("スロッシャー") || name.equals("シェルター") || name.equals("ブラシ") || name.equals("スピナー")
					|| name.equals("マニューバー") || name.equals("ハウンド") || name.equals("スワッパー") || name.equals("ドラグーン")
					|| name.equals("リーラー") || name.equals("バックラー")) {
				switch (name) {
					case "シューター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Shooter", false);
						break;
					case "ブラスター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Blaster", false);
						break;
					case "バーストシューター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Burst", false);
						break;
					case "ローラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Roller", false);
						break;
					case "スロッシャー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Slosher", false);
						break;
					case "シェルター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Kasa", false);
						break;
					case "ブラシ" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Hude", false);
						break;
					case "スピナー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Spinner", false);
						break;
					case "チャージャー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Charger", false);
						break;
					case "マニューバー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Maneu", false);
						break;
					case "ハウンド" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Hound", false);
						break;
					case "スワッパー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Swapper", false);
						break;
					case "ドラグーン" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Funnel", false);
						break;
					case "リーラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Reeler", false);
						break;
					case "バックラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Buckler", false);
						break;
					case "戻る" :
						OpenGUI.openWeaponSelect(player, "Main", "null", false);
						break;
					case "装備選択へ戻る" :
						OpenGUI.equipmentGUI(player, false);
						break;
				}
				return;
			}
			if (name.contains("§6レベル")) {
				SclatUtil.sendMessage("§cレベルが足りないため、まだ選択できません", MessageType.PLAYER, player);
				SclatUtil.playGameSound(player, SoundType.ERROR);
				return;
			}
			// 試しうちモード
			if (conf.config.getString("WorkMode").equals("Trial")) {

				player.getInventory().clear();
				DataMgr.getPlayerData(player).reset();
				DataMgr.getPlayerData(player).isInMatch = (false);
				DataMgr.getPlayerData(player).isJoined = (false);

				for (ArmorStand as : DataMgr.getBeaconMap().values()) {
					if (DataMgr.getBeaconFromplayer(player) == as)
						as.remove();
				}
				for (ArmorStand as : DataMgr.getSprinklerMap().values()) {
					if (DataMgr.getSprinklerFromplayer(player) == as)
						as.remove();
				}

				BukkitRunnable delay = new BukkitRunnable() {
					final Player p = player;
					@Override
					public void run() {
						DataMgr.getPlayerData(p).isInMatch = (true);
						DataMgr.getPlayerData(p).isJoined = (true);
						DataMgr.getPlayerData(p).mainItemGlow = (false);
						DataMgr.getPlayerData(p).tick = 10;
						WeaponClass wc = DataMgr.getWeaponClass(name);
						DataMgr.getPlayerData(p).weaponClass = (wc);
						if (DataMgr.getPlayerData(p).weaponClass.getSubWeaponName().equals("ビーコン"))
							ArmorStandMgr.BeaconArmorStandSetup(p);
						if (DataMgr.getPlayerData(p).weaponClass.getSubWeaponName().equals("スプリンクラー"))
							ArmorStandMgr.SprinklerArmorStandSetup(p);
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.getIsSwap()) {
							Swapper.SwapperRunnable(p);
							if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.getSlidingShootTick() > 1) {
								Shooter.maneuverShootRunnable(p);
								DataMgr.getPlayerData(p).isUsingManeuver = (true);
							}
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Shooter")) {
							Shooter.shooterRunnable(p);
							if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.isManeuver) {
								if (DataMgr.getPlayerData(p).settings.doChargeKeep()) {
									Shooter.maneuverRunnable(p);
								} else {
									Manuber.maneuverRunnable(p);
								}
								Shooter.maneuverShootRunnable(p);
							}
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Reeler")) {
							Shooter.shooterRunnable(p);
							Reeler.reelerRunnable(p);
							Reeler.reelerShootRunnable(p);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Blaster")) {
							if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.isManeuver) {
								Shooter.maneuverRunnable(p);
							}
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Charger")) {
							Charger.ChargerRunnable(p);
							Decoy.DecoyRunnable(p);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Spinner"))
							Spinner.spinnerRunnable(p);
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Roller")) {
							if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.isHude) {
								Brush.HoldRunnable(p);
								Brush.RollPaintRunnable(p);
							} else {
								Roller.holdRunnable(p);
								Roller.rollPaintRunnable(p);
							}
						}

						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Kasa")) {
							Kasa.kasaRunnable(p, false);
						}

						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Camping")) {
							Kasa.kasaRunnable(p, true);
							DataMgr.getPlayerData(p).mainItemGlow = (true);
							WeaponClassMgr.setWeaponClass(p);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Buckler")) {
							Shooter.shooterRunnable(p);
							Buckler.BucklerRunnable(p);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Bucket")) {
							Bucket.BucketHealRunnable(p, 1);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Slosher")) {
							Bucket.BucketHealRunnable(p, 0);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Hound")) {
							Hound.houndRunnable(p);
							Hound.houndEXRunnable(p);
						}
						if (DataMgr.getPlayerData(p).weaponClass.mainWeapon.weaponType.equals("Funnel")) {
							Shooter.shooterRunnable(p);
							Funnel.funnelFloat(p);
						}
						WeaponClassMgr.setWeaponClass(p);
						player.setExp(0.99F);

						// p.setScoreboard(DataMgr.getPlayerData(p).getMatch().getScoreboard());
						// DataMgr.getPlayerData(p).getTeam().getTeam().addEntry(p.getName());

						SPWeaponMgr.SPWeaponRunnable(player);
						SquidMgr.SquidShowRunnable(player);
					}
				};
				delay.runTaskLater(VariablesKt.getPlugin(), 15);
			} else {
				DataMgr.getPlayerData(player).weaponClass = (DataMgr.getWeaponClass(name));
			}
			SclatUtil.sendMessage("ブキ[" + ChatColor.GOLD + name + ChatColor.RESET + "]を選択しました", MessageType.PLAYER,
					player);
		}

		if (event.getView().getTitle().equals("Shop")) {
			if (name.equals("装備選択へ戻る") || name.equals("戻る") || name.equals("シューター") || name.equals("ローラー")
					|| name.equals("チャージャー") || name.equals("ブラスター") || name.equals("バーストシューター")
					|| name.equals("スロッシャー") || name.equals("シェルター") || name.equals("ブラシ") || name.equals("スピナー")
					|| name.equals("マニューバー") || name.equals("ハウンド") || name.equals("スワッパー") || name.equals("ドラグーン")
					|| name.equals("リーラー") || name.equals("バックラー")) {
				switch (name) {
					case "シューター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Shooter", true);
						break;
					case "ブラスター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Blaster", true);
						break;
					case "バーストシューター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Burst", true);
						break;
					case "ローラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Roller", true);
						break;
					case "スロッシャー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Slosher", true);
						break;
					case "シェルター" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Kasa", true);
						break;
					case "ブラシ" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Hude", true);
						break;
					case "スピナー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Spinner", true);
						break;
					case "チャージャー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Charger", true);
						break;
					case "マニューバー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Maneu", true);
						break;
					case "ハウンド" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Hound", true);
						break;
					case "スワッパー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Swapper", true);
						break;
					case "ドラグーン" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Funnel", true);
						break;
					case "リーラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Reeler", true);
						break;
					case "バックラー" :
						OpenGUI.openWeaponSelect(player, "Weapon", "Buckler", true);
						break;
					case "戻る" :
						OpenGUI.openWeaponSelect(player, "Main", "null", true);
						break;
					case "装備選択へ戻る" :
						OpenGUI.equipmentGUI(player, true);
						break;
				}
				return;
			}
			if (name.contains("§6レベル")) {
				SclatUtil.sendMessage("§cレベルが足りないため、まだ購入できません", MessageType.PLAYER, player);
				SclatUtil.playGameSound(player, SoundType.ERROR);
				return;
			}
			if (name.contains("§6ガチャ武器です")) {
				SclatUtil.sendMessage("§cガチャから手に入るよ", MessageType.PLAYER, player);
				SclatUtil.playGameSound(player, SoundType.ERROR);
				return;
			}

			player.closeInventory();
			if (DataMgr.getWeaponClass(name).mainWeapon.islootbox) {

			} else if (PlayerStatusMgr.getMoney(player) >= DataMgr.getWeaponClass(name).mainWeapon.money) {
				PlayerStatusMgr.addWeapon(player, name);
				PlayerStatusMgr.subMoney(player, DataMgr.getWeaponClass(name).mainWeapon.money);
				SclatUtil.sendMessage(ChatColor.GREEN + "購入に成功しました", MessageType.PLAYER, player);
				SclatUtil.playGameSound(player, SoundType.SUCCESS);
				PlayerStatusMgr.sendHologramUpdate(player);
			} else {
				SclatUtil.sendMessage(ChatColor.RED + "お金が足りません", MessageType.PLAYER, player);
				SclatUtil.playGameSound(player, SoundType.ERROR);
			}
		}

		if (event.getView().getTitle().equals("Chose Target")) {
			if (name.equals("§r§6リスポーン地点へジャンプ")) {
				Location loc = Sclat.lobby.clone();
				if (!conf.config.getString("WorkMode").equals("Trial"))
					loc = DataMgr.getPlayerData(player).matchLocation;
				SuperJumpMgr.SuperJumpCollTime(player, loc, false);
			}
			if (name.equals("§r§6ロビーへジャンプ")) {
				String WorldName = conf.config.getString("LobbyJump.WorldName");
				World w = Bukkit.getWorld(WorldName);
				int ix = conf.config.getInt("LobbyJump.X");
				int iy = conf.config.getInt("LobbyJump.Y");
				int iz = conf.config.getInt("LobbyJump.Z");
				Location loc = new Location(w, ix + 0.5, iy, iz + 0.5);
				SuperJumpMgr.SuperJumpCollTime(player, loc, true);
			}
			boolean nearspwan = true;
			Location spawnloc = Sclat.lobby.clone();
			if (!conf.config.getString("WorkMode").equals("Trial"))
				spawnloc = DataMgr.getPlayerData(player).matchLocation;
			if (spawnloc.getWorld() == player.getWorld()) {
				if (player.getLocation().distance(spawnloc) > 10 && !Tutorial.clearList.contains(player))
					if (!Sclat.tutorial) {
						nearspwan = false;
					}
			}
			for (Player p : Sclat.getPlugin(Sclat.class).getServer().getOnlinePlayers()) {
				if (p.getName().equals(name)) {
					if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
						if (p.getGameMode() == GameMode.SPECTATOR) {
							SclatUtil.sendMessage("§c今そのプレイヤーにはジャンプできない！", MessageType.PLAYER, player);
							SclatUtil.playGameSound(player, SoundType.ERROR);
							break;
						}
						SuperJumpMgr.SuperJumpCollTime(player, DataMgr.getPlayerData(p).playerGroundLocation,
								nearspwan);
					}
					if (event.getCurrentItem().getType().equals(Material.IRON_TRAPDOOR))
						SuperJumpMgr.SuperJumpCollTime(player, DataMgr.getBeaconFromplayer(p).getLocation(), nearspwan);
				}
			}
		}

		if (event.getView().getTitle().equals("設定")) {
			if (name.equals("戻る")) {
				OpenGUI.openMenu(player);
				return;
			}

			switch (name) {
				case "メインウエポンのインクエフェクト" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_MainWeaponInk();
					break;
				case "チャージャーのレーザー" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_ChargerLine();
					break;
				case "スペシャルウエポンのエフェクト" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_SPWeapon();
					break;
				case "スペシャルウエポンの範囲エフェクト" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_SPWeaponRegion();
					break;
				case "弾の表示" :
					DataMgr.getPlayerData(player).settings.S_ShowSnowBall();
					break;
				case "BGM" :
					DataMgr.getPlayerData(player).settings.S_PlayBGM();
					break;
				case "投擲武器の視認用エフェクト" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_Bomb();
					break;
				case "爆発エフェクト" :
					DataMgr.getPlayerData(player).settings.S_ShowEffect_BombEx();
					break;
				case "チャージキープ" :
					DataMgr.getPlayerData(player).settings.S_doChargeKeep();
					break;
			}

			OpenGUI.openSettingsUI(player);

			player.playNote(player.getLocation(), Instrument.STICKS, Note.flat(1, Note.Tone.C));

			String B = DataMgr.getPlayerData(player).settings.PlayBGM() ? "1" : "0";
			String E_S = DataMgr.getPlayerData(player).settings.ShowEffect_MainWeaponInk() ? "1" : "0";
			String E_CL = DataMgr.getPlayerData(player).settings.ShowEffect_ChargerLine() ? "1" : "0";
			String E_CS = DataMgr.getPlayerData(player).settings.ShowEffect_SPWeapon() ? "1" : "0";
			String E_RR = DataMgr.getPlayerData(player).settings.ShowEffect_SPWeaponRegion() ? "1" : "0";
			String E_RS = DataMgr.getPlayerData(player).settings.ShowSnowBall() ? "1" : "0";
			// String E_BGM = DataMgr.getPlayerData(player).getSettings().PlayBGM() ? "1" :
			// "0";
			String E_B = DataMgr.getPlayerData(player).settings.ShowEffect_Bomb() ? "1" : "0";
			String E_BEx = DataMgr.getPlayerData(player).settings.ShowEffect_BombEx() ? "1" : "0";
			String ck = DataMgr.getPlayerData(player).settings.doChargeKeep() ? "1" : "0";

			String s_data = B + E_S + E_CL + E_CS + E_RR + E_RS + E_B + E_BEx + ck;

			String uuid = player.getUniqueId().toString();
			conf.getPlayerSettings().set("Settings." + uuid, s_data);
		}

		if (!player.getGameMode().equals(GameMode.CREATIVE))
			event.setCancelled(true);
	}

	@EventHandler
	public void onOpenMainMenu(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();

		if (player.getInventory().getItemInMainHand() == null
				|| player.getInventory().getItemInMainHand().getItemMeta() == null
				|| player.getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null)
			return;

		if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)
				|| action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
			if (player.getInventory().getItemInMainHand().getType().equals(Material.CHEST))
				OpenGUI.openMenu(player);
			switch (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName()) {
				case "スーパージャンプ" :
					OpenGUI.SuperJumpGUI(player);
					break;
				case "§c§n右クリックで退出" :
					BungeeCordMgr.PlayerSendServer(player, "sclat");
					DataMgr.getPlayerData(player).setServerName("Sclat");
					break;
				case "§a§n右クリックで参加" :
					MatchMgr.PlayerJoinMatch(player);
					break;
			}
			if (player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("スーパージャンプ"))
				OpenGUI.SuperJumpGUI(player);

		}
	}
}
