package be4rjp.sclat.api.enchant;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class GlowEnchant extends EnchantmentWrapper {

	private static GlowEnchant glowEnchant = null;

	public GlowEnchant() {
		super("sclatg");
	}

	public boolean canEnchantItem(ItemStack item) {
		return true;
	}

	public boolean conflictsWith(Enchantment other) {
		return false;
	}

	public EnchantmentTarget getItemTarget() {
		return null;
	}

	public int getMaxLevel() {
		return 10;
	}

	public String getName() {
		return "sclatg";
	}

	public int getStartLevel() {
		return 1;
	}

	public ItemStack enchantGlow(ItemStack is) {
		enableGlow();
		is.addEnchantment(glowEnchant, 1);
		return is;
	}

	public ItemStack removeGlow(ItemStack is) {
		enableGlow();
		is.removeEnchantment(glowEnchant);
		return is;
	}

	public Boolean isGlowing(ItemStack is) {
		enableGlow();
		return is.getEnchantments().containsKey(glowEnchant);
	}

	@SuppressWarnings("unchecked")
	public void enableGlow() {
		try {
			if (glowEnchant == null) {
				glowEnchant = new GlowEnchant();
				Field f = Enchantment.class.getDeclaredField("acceptingNew");
				f.setAccessible(true);
				f.set(null, true);
				Field hmapf = Enchantment.class.getDeclaredField("byName");
				hmapf.setAccessible(true);
				Map<String, Enchantment> hmap = (Map<String, Enchantment>) hmapf.get(hmapf);
				if (!hmap.containsKey("sclatg")) {
					Enchantment.registerEnchantment(glowEnchant);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
