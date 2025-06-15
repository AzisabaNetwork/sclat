package be4rjp.sclat.data;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Be4rJP
 */
public class Color {
    private final String colorname;
    private String colorcode;
    private boolean isUsed = false;
    private Material wool;
    private org.bukkit.Color bukkitcolor;
    private Material glass;
    private Material concrete;
    private ItemStack bougu;
    private ChatColor cc;

    public Color(String colorname) {
        this.colorname = colorname;
    }

    public String getColorCode() {
        return colorcode;
    }

    public void setColorCode(String code) {
        colorcode = code;
    }

    public String getColorName() {
        return colorname;
    }

    public boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(boolean isused) {
        isUsed = isused;
    }

    public Material getWool() {
        return wool;
    }

    public void setWool(Material Wool) {
        wool = Wool;
    }

    public Material getGlass() {
        return glass;
    }

    public void setGlass(Material glass) {
        this.glass = glass;
    }

    public Material getConcrete() {
        return concrete;
    }

    public void setConcrete(Material conc) {
        this.concrete = conc;
    }

    public ItemStack getBougu() {
        return bougu;
    }

    public void setBougu(ItemStack bougu) {
        this.bougu = bougu;
    }

    public org.bukkit.Color getBukkitColor() {
        return bukkitcolor;
    }

    public void setBukkitColor(org.bukkit.Color color) {
        this.bukkitcolor = color;
    }

    public ChatColor getChatColor() {
        return this.cc;
    }

    public void setChatColor(ChatColor cc) {
        this.cc = cc;
    }
}
