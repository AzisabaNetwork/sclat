
package be4rjp.sclat.manager;

import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import be4rjp.sclat.weapon.spweapon.*;
import be4rjp.sclat.weapon.subweapon.*;
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
    public static ItemStack getSubWeapon(Player player){
        PlayerData data = DataMgr.getPlayerData(player);
        //player.sendMessage(data.getWeaponClass().getSubWeaponName());
        ItemStack is = null;
        ItemMeta ism = null;
        
        switch (data.getWeaponClass().getSubWeaponName()) {
            case "スプラッシュボム":
                is = new ItemStack(data.getTeam().getTeamColor().getGlass());
                ism = is.getItemMeta();
                ism.setDisplayName("スプラッシュボム");
                break;
            case "クイックボム":
                is = new ItemStack(data.getTeam().getTeamColor().getWool());
                ism = is.getItemMeta();
                ism.setDisplayName("クイックボム");
                break;
            case "フローターボム":
                is = new ItemStack(data.getTeam().getTeamColor().getWool());
                ism = is.getItemMeta();
                ism.setDisplayName("フローターボム");
                break;
            case "ブーメランボム":
                is = new ItemStack(Material.IRON_NUGGET);
                ism = is.getItemMeta();
                ism.setDisplayName("ブーメランボム");
                break;
            case "センサー":
                is = new ItemStack(Material.DISPENSER);
                ism = is.getItemMeta();
                ism.setDisplayName("センサー");
                break;
            case "ポイズン":
                is = new ItemStack(Material.PRISMARINE);
                ism = is.getItemMeta();
                ism.setDisplayName("ポイズン");
                break;
            case "キューバンボム":
                is = new ItemStack(data.getTeam().getTeamColor().getConcrete());
                ism = is.getItemMeta();
                ism.setDisplayName("キューバンボム");
                break;
            case "ビーコン":
                is = new ItemStack(Material.IRON_TRAPDOOR);
                ism = is.getItemMeta();
                ism.setDisplayName("ビーコン");
                break;
            case "スプリンクラー":
                is = new ItemStack(Material.BIRCH_FENCE_GATE);
                ism = is.getItemMeta();
                ism.setDisplayName("スプリンクラー");
                break;
            case "スプラッシュシールド":
                is = new ItemStack(Material.ACACIA_FENCE);
                ism = is.getItemMeta();
                ism.setDisplayName("スプラッシュシールド");
                break;
            case "カーリングボム":
                is = new ItemStack(Material.QUARTZ_SLAB);
                ism = is.getItemMeta();
                ism.setDisplayName("カーリングボム");
                break;
            case "トラップ":
                is = new ItemStack(Material.MUSIC_DISC_STAL);
                ism = is.getItemMeta();
                ism.setDisplayName("トラップ");
                break;
        }
        is.setItemMeta(ism);
        //player.getInventory().setItem(2, is);  
        return is;
    }
    
    public static void UseSubWeapon(Player player, String name){
        PlayerData data = DataMgr.getPlayerData(player);
        if(!data.getCanUseSubWeapon()) return;
        if(player.getGameMode().equals(GameMode.SPECTATOR)) return;
        
        switch (name) {
            case "右クリックで弾を発射":
                JetPack.ShootJetPack(player);
                data.setCanUseSubWeapon(false);
                break;
            case "右クリックで発射！":
                SuperShot.Shot(player);
                data.setCanUseSubWeapon(false);
                break;
            case "右クリックで斬撃、シフトで防御":
                SwordMord.AttackSword(player);
                data.setCanUseSubWeapon(false);
                break;
            case "Quadro-BLUE":
                QuadroArms.QuadroCooltime(player,1);
                data.setCanUseSubWeapon(false);
                break;
            case "Quadro-GREEN":
                QuadroArms.QuadroCooltime(player,2);
                data.setCanUseSubWeapon(false);
                break;
            case "Quadro-RED":
                QuadroArms.QuadroCooltime(player,3);
                data.setCanUseSubWeapon(false);
                break;
            case "Quadro-WHITE":
                QuadroArms.QuadroCooltime(player,4);
                data.setCanUseSubWeapon(false);
                break;
            case "右クリックで射撃!":
                if(data.getSettings().ShowEffect_ChargerLine()) {
                    LitterFiveG.Shoot_LitterFiveG(player);
                    data.setCanUseSubWeapon(false);
                }else{
                    LitterFiveG.Charge_LitterFiveG(player);
                    data.setCanUseSubWeapon(true);
                }
                break;
        }
        
        if(data.getIsUsingJetPack()) return;
        
        switch (name) {
            case "スプラッシュボム":
                SplashBomb.SplashBomRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "クイックボム":
                QuickBomb.QuickBomRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "フローターボム":
                FloaterBomb.FloaterBombRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "センサー":
                Sensor.SensorRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "ポイズン":
                Poison.PoisonRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "キューバンボム":
                KBomb.KBomRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "ビーコン":
                Beacon.setBeacon(player);
                data.setCanUseSubWeapon(false);
                break;
            case "スプリンクラー":
                Sprinkler.SprinklerRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "スプラッシュシールド":
                SplashShield.SplashShieldThrowRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "カーリングボム":
                CurlingBomb.CurlingBombRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "ブーメランボム":
                Boomerang.BoomerangRunnable(player);
                data.setCanUseSubWeapon(false);
                break;
            case "トラップ":
                Trap.useTrap(player);
                data.setCanUseSubWeapon(false);
                break;
        }
    }
}
