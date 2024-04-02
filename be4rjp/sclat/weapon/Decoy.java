package be4rjp.sclat.weapon;

import be4rjp.sclat.Main;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.data.PlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.*;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Decoy {
    public static void DecoyRunnable(Player player){
        BukkitRunnable delay = new BukkitRunnable(){
            Player p = player;
            boolean dc_recharge=true;

            @Override
            public void run(){
                PlayerData data = DataMgr.getPlayerData(p);

                if(!data.isInMatch() || !p.isOnline()){
                    cancel();
                    return;
                }

                    if (data.getIsSneaking() && dc_recharge == true && player.getGameMode() != GameMode.SPECTATOR) {
                        dc_recharge = false;
                        //createDecoy(p, p.getName(), p.getLocation());
                        DecoyShot(p);
                        BukkitRunnable task = new BukkitRunnable() {//クールタイムを管理しています
                            @Override
                            public void run() {
                                dc_recharge = true;
                            }
                        };
                        //Decoyset.runTaskLater(Main.getPlugin(), 5);
                        task.runTaskLater(Main.getPlugin(), 95);
                    }
            }
        };
        delay.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void createDecoy(Player player1, String npcName1, Location location1) {
        BukkitRunnable task = new BukkitRunnable(){
            EntityPlayer npc;

            int s = 0;

            Player player = player1;
            String npcName = npcName1;
            Location location = location1;

            @Override
            public void run(){
                if(s == 0){
                    location.setYaw(location1.getYaw());

                    MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                    WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
                    GameProfile gameProfile = new GameProfile(player.getUniqueId(), npcName);

                    npc = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));

                    //見えないところにスポーンさせて、クライアントにスキンを先に読み込ませる
                    npc.setLocation(location.getX(), location.getY() - 20, location.getZ(), player1.getEyeLocation().getYaw(), 0);
                    npc.getDataWatcher().set(DataWatcherRegistry.a.a(15), (byte)127);

                    for(Player p : Main.getPlugin(Main.class).getServer().getOnlinePlayers()){
                        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
                        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
                        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
                        connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));
                    }
                }
                if(s == 0){
                    npc.setLocation(location.getX(), location.getY(), location.getZ(), player1.getEyeLocation().getYaw(), 0);
                    for(Player p : Main.getPlugin(Main.class).getServer().getOnlinePlayers()){
                        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
                        connection.sendPacket(new PacketPlayOutEntityTeleport(npc));
                        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) ((player1.getEyeLocation().getYaw() * 256.0F) / 360.0F)));
                        connection.sendPacket(new PacketPlayOutEntityEquipment(npc.getBukkitEntity().getEntityId(), EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(DataMgr.getPlayerData(player).getWeaponClass().getMainWeapon().getWeaponIteamStack())));
                        connection.sendPacket(new PacketPlayOutEntityEquipment(npc.getBukkitEntity().getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(DataMgr.getPlayerData(player).getTeam().getTeamColor().getBougu())));
                        connection.sendPacket(new PacketPlayOutAnimation(npc, 0));
                    }

                }
                if(s == 5){
                    for(Player p : Main.getPlugin(Main.class).getServer().getOnlinePlayers()){
                        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
                        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getBukkitEntity().getEntityId()));
                    }
                    cancel();
                }
                s++;
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 20);

    }
    public static void DecoyShot(Player player){
        BukkitRunnable task = new BukkitRunnable(){
            Player p = player;

            ArmorStand as1;
            int c = 0;
            @Override
            public void run(){
                try{
                    if(c == 0){
                        as1 = player.getWorld().spawn(player.getLocation().add(0, 1.6, 0), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setSmall(true);
                        });
                        as1.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(2.0));
                    }

                    //デコイショットの視認用エフェクト
                        if(DataMgr.getPlayerData(player).getSettings().ShowEffect_Bomb()){
                            if(player.getWorld() == as1.getLocation().getWorld()) {
                                if (player.getLocation().distanceSquared(as1.getLocation()) < Main.PARTICLE_RENDER_DISTANCE_SQUARED) {
                                    Particle.DustOptions dustOptions = new Particle.DustOptions(DataMgr.getPlayerData(p).getTeam().getTeamColor().getBukkitColor(), 1);
                                    player.spawnParticle(Particle.REDSTONE, as1.getLocation(), 1, 0, 0, 0, 50, dustOptions);
                                }
                            }
                        }

                    c++;

                    if(c > 500){
                        as1.remove();
                        cancel();
                        return;
                    }

                    if(as1.isOnGround()) {
                        createDecoy(p, p.getName(),as1.getLocation());
                        as1.remove();
                        cancel();
                        return;
                    }
                }catch(Exception e){
                    as1.remove();
                    cancel();
                    Main.getPlugin().getLogger().warning(e.getMessage());
                }
            }
        };
        task.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}
