
package be4rjp.sclat.manager;

import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.weapon.spweapon.JetPack;
import be4rjp.sclat.weapon.spweapon.LitterFiveG;
import be4rjp.sclat.weapon.spweapon.QuadroArms;
import be4rjp.sclat.weapon.spweapon.SuperShot;
import be4rjp.sclat.weapon.spweapon.SwordMord;
import be4rjp.sclat.weapon.subweapon.Beacon;
import be4rjp.sclat.weapon.subweapon.Boomerang;
import be4rjp.sclat.weapon.subweapon.CurlingBomb;
import be4rjp.sclat.weapon.subweapon.FloaterBomb;
import be4rjp.sclat.weapon.subweapon.KBomb;
import be4rjp.sclat.weapon.subweapon.Poison;
import be4rjp.sclat.weapon.subweapon.QuickBomb;
import be4rjp.sclat.weapon.subweapon.Sensor;
import be4rjp.sclat.weapon.subweapon.SplashBomb;
import be4rjp.sclat.weapon.subweapon.SplashShield;
import be4rjp.sclat.weapon.subweapon.Sprinkler;
import be4rjp.sclat.weapon.subweapon.Trap;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Be4rJP
 */
public class SubWeaponMgr {
	public static ItemStack getSubWeapon(Player player) {
		PlayerData data = DataMgr.getPlayerData(player);
		// player.sendMessage(data.weaponClass.getSubWeaponName());
		ItemStack is = null;
		ItemMeta ism = null;

		switch (data.weaponClass.getSubWeaponName()) {
			case "スプラッシュボム" :
				is = new ItemStack(data.team.getTeamColor().glass);
				ism = is.getItemMeta();
				ism.setDisplayName("スプラッシュボム");
				break;
			case "クイックボム" :
				is = new ItemStack(data.team.getTeamColor().wool);
				ism = is.getItemMeta();
				ism.setDisplayName("クイックボム");
				break;
			case "フローターボム" :
				is = new ItemStack(data.team.getTeamColor().wool);
				ism = is.getItemMeta();
				ism.setDisplayName("フローターボム");
				break;
			case "ブーメランボム" :
				is = new ItemStack(Material.IRON_NUGGET);
				ism = is.getItemMeta();
				ism.setDisplayName("ブーメランボム");
				break;
			case "センサー" :
				is = new ItemStack(Material.DISPENSER);
				ism = is.getItemMeta();
				ism.setDisplayName("センサー");
				break;
			case "ポイズン" :
				is = new ItemStack(Material.PRISMARINE);
				ism = is.getItemMeta();
				ism.setDisplayName("ポイズン");
				break;
			case "キューバンボム" :
				is = new ItemStack(data.team.getTeamColor().concrete);
				ism = is.getItemMeta();
				ism.setDisplayName("キューバンボム");
				break;
			case "ビーコン" :
				is = new ItemStack(Material.IRON_TRAPDOOR);
				ism = is.getItemMeta();
				ism.setDisplayName("ビーコン");
				break;
			case "スプリンクラー" :
				is = new ItemStack(Material.BIRCH_FENCE_GATE);
				ism = is.getItemMeta();
				ism.setDisplayName("スプリンクラー");
				break;
			case "スプラッシュシールド" :
				is = new ItemStack(Material.ACACIA_FENCE);
				ism = is.getItemMeta();
				ism.setDisplayName("スプラッシュシールド");
				break;
			case "カーリングボム" :
				is = new ItemStack(Material.QUARTZ_SLAB);
				ism = is.getItemMeta();
				ism.setDisplayName("カーリングボム");
				break;
			case "トラップ" :
				is = new ItemStack(Material.MUSIC_DISC_STAL);
				ism = is.getItemMeta();
				ism.setDisplayName("トラップ");
				break;
		}
		is.setItemMeta(ism);
		// player.getInventory().setItem(2, is);
		return is;
	}

	public static void UseSubWeapon(Player player, String name) {
		PlayerData data = DataMgr.getPlayerData(player);
		if (!data.canUseSubWeapon)
			return;
		if (player.getGameMode().equals(GameMode.SPECTATOR))
			return;

		switch (name) {
			case "右クリックで弾を発射" :
				JetPack.shootJetPack(player);
				data.canUseSubWeapon = (false);
				break;
			case "右クリックで発射！" :
				SuperShot.shot(player);
				data.canUseSubWeapon = (false);
				break;
			case "右クリックで斬撃、シフトで防御" :
				SwordMord.attackSword(player);
				data.canUseSubWeapon = (false);
				break;
			case "Quadro-BLUE" :
				QuadroArms.quadroCooltime(player, 1);
				data.canUseSubWeapon = (false);
				break;
			case "Quadro-GREEN" :
				QuadroArms.quadroCooltime(player, 2);
				data.canUseSubWeapon = (false);
				break;
			case "Quadro-RED" :
				QuadroArms.quadroCooltime(player, 3);
				data.canUseSubWeapon = (false);
				break;
			case "Quadro-WHITE" :
				QuadroArms.quadroCooltime(player, 4);
				data.canUseSubWeapon = (false);
				break;
			case "右クリックで射撃!" :
				if (data.settings.ShowEffect_ChargerLine()) {
					LitterFiveG.shootLitterFiveG(player);
					data.canUseSubWeapon = (false);
				} else {
					LitterFiveG.chargeLitterFiveG(player);
					data.canUseSubWeapon = (true);
				}
				break;
		}

		if (data.isUsingJetPack)
			return;

		switch (name) {
			case "スプラッシュボム" :
				SplashBomb.SplashBomRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "クイックボム" :
				QuickBomb.quickBomRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "フローターボム" :
				FloaterBomb.FloaterBombRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "センサー" :
				Sensor.sensorRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "ポイズン" :
				Poison.poisonRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "キューバンボム" :
				KBomb.kBomRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "ビーコン" :
				Beacon.setBeacon(player);
				data.canUseSubWeapon = (false);
				break;
			case "スプリンクラー" :
				Sprinkler.SprinklerRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "スプラッシュシールド" :
				SplashShield.splashShieldThrowRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "カーリングボム" :
				CurlingBomb.curlingBombRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "ブーメランボム" :
				Boomerang.boomerangRunnable(player);
				data.canUseSubWeapon = (false);
				break;
			case "トラップ" :
				Trap.useTrap(player);
				data.canUseSubWeapon = (false);
				break;
		}
	}
}
