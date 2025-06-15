package be4rjp.sclat.data;

import net.minecraft.server.v1_14_R1.ItemStack;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * @author Be4rJP
 */
public class PlayerData {
    private Player player;
    private PlayerSettings settings;
    private String classname;
    private Match match;
    private Team team;
    private boolean inmatch = false;
    private WeaponClass weaponclass;
    private MainWeapon mainweapon;
    private Location matchloc;
    private int playernumber = 0;
    private boolean canshoot = true;
    private int tick = 0;
    private boolean issquid = false;
    private boolean isonink = false;
    private boolean isHolding = false;
    private boolean isjoined = false;
    private boolean canpaint = false;
    private int killcount = 0;
    private int paintcount = 0;
    private double armor = 0;
    private int spgauge = 0;
    private boolean bombrush = false;
    private boolean sp = false;
    private boolean canusesub = true;
    private boolean charge = false;
    private boolean poison = false;
    private boolean canrollershoot = true;
    private Location maploc;
    private boolean isusingsp = false;
    private boolean cancharge = true;
    private boolean isdead = false;
    private String server = "";
    private ItemStack is;
    private boolean isonpath = false;
    private boolean isUsingMM = false;
    private boolean isPoisonCoolTime = false;
    private boolean isSneak = false;
    private boolean isUsingManeuver = false;
    private boolean issliding = false;
    private int gearNumber = 0;
    private boolean isUsingJetPack = false;
    private boolean doChargeKeep = false;
    private boolean MItemGlow = false;
    private boolean amehurashi = false;
    private boolean isUsingTyakuti = false;
    private boolean isJumping = false;
    private boolean isCanFly = false;
    private int trapCount = 0;
    private Location playerGroundLocation;
    private boolean isUsingSS = false;
    private Vector vehicleVector = new Vector(0, 0, 0);
    private double speed = 0;
    private float fov = 0.1F;
    private ArrayList<ArmorStand> Armorlist = new ArrayList<>();
    private Player lastAttack = player;
    private boolean stoprun = false;


    public PlayerData(Player player) {
        this.player = player;
    }

    public PlayerSettings getSettings() {
        return settings;
    }

    public void setSettings(PlayerSettings settings) {
        this.settings = settings;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    //public MainWeapon getMainWeapon(){return mainweapon;}

    public boolean isInMatch() {
        return inmatch;
    }

    public boolean getCanShoot() {
        return canshoot;
    }

    public void setCanShoot(boolean canshoot) {
        this.canshoot = canshoot;
    }

    public WeaponClass getWeaponClass() {
        return weaponclass;
    }

    public void setWeaponClass(WeaponClass weaponclass) {
        this.weaponclass = weaponclass;
    }

    public Location getMatchLocation() {
        return this.matchloc;
    }

    public void setMatchLocation(Location l) {
        this.matchloc = l;
    }

    public int getPlayerNumber() {
        return this.playernumber;
    }

    public void setPlayerNumber(int i) {
        this.playernumber = i;
    }

    public int getTick() {
        return this.tick;
    }

    public void setTick(int i) {
        this.tick = i;
    }

    public boolean getIsSquid() {
        return this.issquid;
    }

    public void setIsSquid(boolean is) {
        this.issquid = is;
    }

    public boolean getIsOnInk() {
        return this.isonink;
    }

    public void setIsOnInk(boolean is) {
        this.isonink = is;
    }

    public boolean getIsHolding() {
        return this.isHolding;
    }

    public void setIsHolding(boolean is) {
        this.isHolding = is;
    }

    public boolean getIsJoined() {
        return this.isjoined;
    }

    public void setIsJoined(boolean is) {
        this.isjoined = is;
    }

    public boolean getCanPaint() {
        return this.canpaint;
    }

    public void setCanPaint(boolean is) {
        this.canpaint = is;
    }

    public int getKillCount() {
        return this.killcount;
    }

    public int getPaintCount() {
        return this.paintcount;
    }

    public double getArmor() {
        return this.armor;
    }

    public void setArmor(double armor) {
        this.armor = armor;
    }

    public int getSPGauge() {
        return this.spgauge;
    }

    public void setSPGauge(int spgauge) {
        this.spgauge = spgauge;
    }

    public boolean getIsBombRush() {
        return this.bombrush;
    }

    public void setIsBombRush(boolean is) {
        this.bombrush = is;
    }

    public boolean getIsSP() {
        return this.sp;
    }

    public void setIsSP(boolean is) {
        this.sp = is;
    }

    public boolean getCanUseSubWeapon() {
        return this.canusesub;
    }

    public void setCanUseSubWeapon(boolean is) {
        this.canusesub = is;
    }

    public boolean getIsCharging() {
        return this.charge;
    }

    public void setIsCharging(boolean is) {
        this.charge = is;
    }

    public boolean getPoison() {
        return this.poison;
    }

    public void setPoison(boolean is) {
        this.poison = is;
    }

    public boolean getCanRollerShoot() {
        return this.canrollershoot;
    }

    public void setCanRollerShoot(boolean is) {
        this.canrollershoot = is;
    }

    public Location getPlayerMapLoc() {
        return this.maploc;
    }

    public void setPlayerMapLoc(Location loc) {
        this.maploc = loc;
    }

    public boolean getIsUsingSP() {
        return this.isusingsp;
    }

    public void setIsUsingSP(boolean is) {
        this.isusingsp = is;
    }

    public boolean getCanCharge() {
        return this.cancharge;
    }

    public void setCanCharge(boolean is) {
        this.cancharge = is;
    }

    public boolean getIsDead() {
        return this.isdead;
    }

    public void setIsDead(boolean is) {
        this.isdead = is;
    }

    public String getServername() {
        return this.server;
    }

    public ItemStack getPlayerHead() {
        return this.is;
    }

    public void setPlayerHead(ItemStack is) {
        this.is = is;
    }

    public boolean getIsOnPath() {
        return this.isonpath;
    }

    public void setIsOnPath(boolean is) {
        this.isonpath = is;
    }

    //public void setMainWeapon(MainWeapon mainweapon){this.mainweapon = mainweapon;}

    public boolean getIsUsingMM() {
        return this.isUsingMM;
    }

    public void setIsUsingMM(boolean is) {
        this.isUsingMM = is;
    }

    public boolean getIsPoisonCoolTime() {
        return this.isPoisonCoolTime;
    }

    public void setIsPoisonCoolTime(boolean is) {
        this.isPoisonCoolTime = is;
    }

    public boolean getIsSneaking() {
        return this.isSneak;
    }

    public void setIsSneaking(boolean is) {
        this.isSneak = is;
    }

    public boolean getIsUsingManeuver() {
        return this.isUsingManeuver;
    }

    public void setIsUsingManeuver(boolean is) {
        this.isUsingManeuver = is;
    }

    public boolean getIsSliding() {
        return this.issliding;
    }

    public void setIsSliding(boolean is) {
        this.issliding = is;
    }

    public int getGearNumber() {
        return this.gearNumber;
    }

    public void setGearNumber(int i) {
        this.gearNumber = i;
    }

    public boolean getIsUsingJetPack() {
        return this.isUsingJetPack;
    }

    public void setIsUsingJetPack(boolean is) {
        this.isUsingJetPack = is;
    }

    public boolean getDoChargeKeep() {
        return this.doChargeKeep;
    }

    public boolean getMainItemGlow() {
        return this.MItemGlow;
    }

    public void setMainItemGlow(boolean is) {
        this.MItemGlow = is;
    }

    public boolean getIsUsingAmehurashi() {
        return this.amehurashi;
    }

    public void setIsUsingAmehurashi(boolean is) {
        this.amehurashi = is;
    }

    public boolean getIsUsingTyakuti() {
        return this.isUsingTyakuti;
    }

    public void setIsUsingTyakuti(boolean is) {
        this.isUsingTyakuti = is;
    }

    public int getTrapCount() {
        return this.trapCount;
    }

    public Location getPlayerGroundLocation() {
        return this.playerGroundLocation;
    }

    public void setPlayerGroundLocation(Location loc) {
        this.playerGroundLocation = loc;
    }

    public boolean getIsUsingSS() {
        return this.isUsingSS;
    }

    public void setIsUsingSS(boolean is) {
        this.isUsingSS = is;
    }

    public Vector getVehicleVector() {
        return this.vehicleVector;
    }

    public void setVehicleVector(Vector vec) {
        this.vehicleVector = vec;
    }

    public boolean getIsJumping() {
        return this.isJumping;
    }

    public void setIsJumping(boolean is) {
        this.isJumping = is;
    }

    public boolean getCanFly() {
        return this.isCanFly;
    }

    public void setCanFly(boolean is) {
        this.isCanFly = is;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public ArmorStand getArmorlist(int n) {
        return this.Armorlist.get(n);
    }

    public Player getLastAttack() {
        return this.lastAttack;
    }

    public void setLastAttack(Player p) {
        this.lastAttack = p;
    }

    public boolean getStoprun() {
        return this.stoprun;
    }

    public void setStoprun(boolean b) {
        this.stoprun = b;
    }

    public void setIsInMatch(boolean isinmatch) {
        this.inmatch = isinmatch;
    }

    public void setServerName(String server) {
        this.server = server;
    }

    public void setArmorlist(ArmorStand n) {
        this.Armorlist.add(n);
    }

    public void subArmorlist(ArmorStand n) {
        this.Armorlist.remove(n);
    }

    public void reflectionDoChargeKeep() {
        this.doChargeKeep = !this.doChargeKeep;
    }


    public void addKillCount() {
        this.killcount++;
    }

    public void addPaintCount() {
        this.paintcount++;
    }

    public void addSPGauge() {
        this.spgauge++;
    }

    public void resetSPGauge() {
        this.spgauge = 0;
    }

    public void addTrapCount() {
        this.trapCount++;
    }

    public void reset() {
        this.inmatch = false;
        this.playernumber = 0;
        this.tick = 0;
        this.inmatch = false;
        this.isonink = false;
        this.isHolding = false;
        this.isjoined = false;
        this.canpaint = false;
        this.canshoot = true;
        this.killcount = 0;
        this.paintcount = 0;
        this.armor = 0;
        this.spgauge = 0;
        this.sp = false;
        this.canusesub = true;
        this.charge = false;
        this.poison = false;
        this.canrollershoot = true;
        this.cancharge = true;
        this.isdead = false;
        this.isonpath = false;
        this.isSneak = false;
        this.isUsingManeuver = false;
        this.issliding = false;
        this.isUsingJetPack = false;
        this.MItemGlow = false;
        this.amehurashi = false;
        this.isUsingTyakuti = false;
        this.trapCount = 0;
        this.isUsingSS = false;
        this.vehicleVector = new Vector(0, 0, 0);
        this.isJumping = false;
        this.isCanFly = false;
        this.speed = 0;
        this.fov = 0.1F;
        this.Armorlist = new ArrayList<>();
        this.stoprun = false;
    }

}
