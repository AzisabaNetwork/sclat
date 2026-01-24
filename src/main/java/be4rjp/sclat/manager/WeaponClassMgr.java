
package be4rjp.sclat.manager;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.api.config.WeaponConfig;
import be4rjp.sclat.api.player.PlayerData;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.WeaponClass;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be4rjp.sclat.Sclat.conf;

/**
 *
 * @author Be4rJP
 */
public class WeaponClassMgr {
	private static final Logger logger = LoggerFactory.getLogger(WeaponClassMgr.class);

	public synchronized static void WeaponClassSetup() {
		ConfigurationSection weaponClassSection = conf.getClassConfig().getConfigurationSection("WeaponClass");
		if(weaponClassSection == null) {
			logger.warn("weaponClassSection is null");
			return;
		}

		for (String classname : weaponClassSection.getKeys(false)) {
			ConfigurationSection weaponSection = weaponClassSection.getConfigurationSection(classname);
			if(weaponSection == null) {
				logger.warn("weaponSection of {} is null", classname);
				continue;
			}

			WeaponClass wc = WeaponConfig.parseSection(classname, weaponSection);
			DataMgr.setWeaponClass(classname, wc);
		}
	}

	public static void setWeaponClass(Player player) {
		player.getInventory().clear();
		PlayerData data = DataMgr.getPlayerData(player);
		ItemStack main = data.getWeaponClass().getMainWeapon().getWeaponIteamStack().clone();
		if (data.getMainItemGlow()) {
			Sclat.glow.enchantGlow(main);
			main.addEnchantment(Sclat.glow, 1);
		}
		player.getInventory().setItem(0, main);
		if (data.getWeaponClass().getMainWeapon().getIsManeuver())
			player.getInventory().setItem(40, data.getWeaponClass().getMainWeapon().getWeaponIteamStack().clone());
		ItemStack is = SubWeaponMgr.getSubWeapon(player);
		player.getInventory().setItem(2, is);
		ItemStack co = new ItemStack(Material.BOOK);
		ItemMeta meta = co.getItemMeta();
		meta.setDisplayName("スーパージャンプ");
		co.setItemMeta(meta);
		player.getInventory().setItem(6, co);
		if (!data.getIsSquid())
			player.getEquipment().setHelmet(DataMgr.getPlayerData(player).getTeam().getTeamColor().getBougu());

		if (data.getSPGauge() == 100)
			SPWeaponMgr.setSPWeapon(player);

		if (conf.getConfig().getString("WorkMode").equals("Trial") && !Sclat.tutorial) {
			ItemStack join = new ItemStack(Material.CHEST);
			ItemMeta joinmeta = join.getItemMeta();
			joinmeta.setDisplayName(ChatColor.GOLD + "右クリックでメインメニューを開く");
			join.setItemMeta(joinmeta);
			player.getInventory().setItem(7, join);
		}
	}

}
