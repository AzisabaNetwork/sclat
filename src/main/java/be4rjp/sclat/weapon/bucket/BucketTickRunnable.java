package be4rjp.sclat.weapon.bucket;

import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.PaintMgr;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.function.Supplier;

public class BucketTickRunnable extends BukkitRunnable {
    int i = 0;
    String name;
    int tick;
    Snowball inkball;
    Player p;
    Supplier<Snowball> snowballSupplier;
    boolean addedFallVec = false;
    Vector fallvec = new Vector(inkball.getVelocity().getX(), inkball.getVelocity().getY(),
            inkball.getVelocity().getZ())
            .multiply(DataMgr.getPlayerData(p).getWeaponClass().getMainWeapon().getShootSpeed() / 17);
    public BucketTickRunnable(String name, Player player, Snowball inkball, int tick, Supplier<Snowball> snowballSupplier) {
        this.name = name;
        this.p = player;
        this.inkball = inkball;
        this.tick = tick;
        this.snowballSupplier = snowballSupplier;
    }
    @Override
    public void run() {
        inkball = DataMgr.getMainSnowballNameMap().get(name);

        if (!inkball.equals(snowballSupplier.get())) {
            i += DataMgr.getSnowballHitCount(name) - 1;
            DataMgr.setSnowballHitCount(name, 0);
        }
        if (i != 0) {
            for (Player target : Sclat.getPlugin().getServer().getOnlinePlayers()) {
                if (target.getWorld() != p.getWorld())
                    continue;
                if (!DataMgr.getPlayerData(target).getSettings().ShowEffect_MainWeaponInk())
                    continue;
                if (target.getWorld() == inkball.getWorld()) {
                    if (target.getLocation()
                            .distanceSquared(inkball.getLocation()) < Sclat.PARTICLE_RENDER_DISTANCE_SQUARED) {
                        org.bukkit.block.data.BlockData bd = DataMgr.getPlayerData(p).getTeam().getTeamColor()
                                .getWool().createBlockData();
                        target.spawnParticle(org.bukkit.Particle.BLOCK_DUST, inkball.getLocation(), 1, 0, 0, 0,
                                1, bd);
                    }
                }
            }
        }

        if (i >= tick && !addedFallVec) {
            inkball.setVelocity(fallvec);
            addedFallVec = true;
        }
        if (i >= tick && i <= tick + 15)
            inkball.setVelocity(inkball.getVelocity().add(new Vector(0, -0.1, 0)));
        if (i != tick)
            PaintMgr.PaintHightestBlock(inkball.getLocation(), p, true, true);
        if (inkball.isDead())
            cancel();

        i++;
    }
}
