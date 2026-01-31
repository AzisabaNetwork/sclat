package be4rjp.sclat.api.player;

import be4rjp.sclat.api.team.Team;
import be4rjp.sclat.data.MainWeapon;
import be4rjp.sclat.data.Match;
import be4rjp.sclat.data.WeaponClass;
import java.util.ArrayList;
import net.minecraft.server.v1_14_R1.ItemStack;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Be4rJP
 */
public class PlayerData {
	public Player player;
	public PlayerSettings settings;
	public String classname;
	public Match match;
	public Team team;
	public boolean inmatch = false;
	public WeaponClass weaponclass;
	public MainWeapon mainweapon;
	public Location matchloc;
	public int playernumber = 0;
	public boolean canshoot = true;
	public int tick = 0;
	public boolean issquid = false;
	public boolean isonink = false;
	public boolean isHolding = false;
	public boolean isjoined = false;
	public boolean canpaint = false;
	public int killcount = 0;
	public int paintcount = 0;
	public double armor = 0;
	public int spgauge = 0;
	public boolean bombrush = false;
	public boolean sp = false;
	public boolean canusesub = true;
	public boolean charge = false;
	public boolean poison = false;
	public boolean canrollershoot = true;
	public Location maploc;
	public boolean isusingsp = false;
	public boolean cancharge = true;
	public boolean isdead = false;
	public String server = "";
	public ItemStack is;
	public boolean isonpath = false;
	public boolean isUsingMM = false;
	public boolean isPoisonCoolTime = false;
	public boolean isSneak = false;
	public boolean isUsingManeuver = false;
	public boolean issliding = false;
	public int gearNumber = 0;
	public boolean isUsingJetPack = false;
	public boolean doChargeKeep = false;
	public boolean MItemGlow = false;
	public boolean amehurashi = false;
	public boolean isUsingTyakuti = false;
	public boolean isJumping = false;
	public boolean isCanFly = false;
	public int trapCount = 0;
	public Location playerGroundLocation;
	public boolean isUsingSS = false;
	public Vector vehicleVector = new Vector(0, 0, 0);
	public double speed = 0;
	public float fov = 0.1F;
	public ArrayList<ArmorStand> Armorlist = new ArrayList<>();
	public Player lastAttack = player;
	public boolean stoprun = false;

	public PlayerData(Player player) {
		this.player = player;
	}

	public PlayerSettings getSettings() {
		return settings;
	}

	public Match getMatch() {
		return match;
	}

	public Team getTeam() {
		return team;
	}

	public boolean isInMatch() {
		return inmatch;
	}

	public boolean getCanShoot() {
		return canshoot;
	}

	public WeaponClass getWeaponClass() {
		return weaponclass;
	}

	// public MainWeapon getMainWeapon(){return mainweapon;}

	public Location getMatchLocation() {
		return this.matchloc;
	}

	public int getPlayerNumber() {
		return this.playernumber;
	}

	public int getTick() {
		return this.tick;
	}

	public boolean getIsSquid() {
		return this.issquid;
	}

	public boolean getIsOnInk() {
		return this.isonink;
	}

	public boolean getIsHolding() {
		return this.isHolding;
	}

	public boolean getIsJoined() {
		return this.isjoined;
	}

	public boolean getCanPaint() {
		return this.canpaint;
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

	public int getSPGauge() {
		return this.spgauge;
	}

	public boolean getIsBombRush() {
		return this.bombrush;
	}

	public boolean getIsSP() {
		return this.sp;
	}

	public boolean getCanUseSubWeapon() {
		return this.canusesub;
	}

	public boolean getIsCharging() {
		return this.charge;
	}

	public boolean getPoison() {
		return this.poison;
	}

	public boolean getCanRollerShoot() {
		return this.canrollershoot;
	}

	public Location getPlayerMapLoc() {
		return this.maploc;
	}

	public boolean getIsUsingSP() {
		return this.isusingsp;
	}

	public boolean getCanCharge() {
		return this.cancharge;
	}

	public boolean getIsDead() {
		return this.isdead;
	}

	public String getServername() {
		return this.server;
	}

	public ItemStack getPlayerHead() {
		return this.is;
	}

	public boolean getIsOnPath() {
		return this.isonpath;
	}

	public boolean getIsUsingMM() {
		return this.isUsingMM;
	}

	public boolean getIsPoisonCoolTime() {
		return this.isPoisonCoolTime;
	}

	public boolean getIsSneaking() {
		return this.isSneak;
	}

	public boolean getIsUsingManeuver() {
		return this.isUsingManeuver;
	}

	public boolean getIsSliding() {
		return this.issliding;
	}

	public int getGearNumber() {
		return this.gearNumber;
	}

	public boolean getIsUsingJetPack() {
		return this.isUsingJetPack;
	}

	public boolean getDoChargeKeep() {
		return this.doChargeKeep;
	}

	public boolean getMainItemGlow() {
		return this.MItemGlow;
	}

	public boolean getIsUsingAmehurashi() {
		return this.amehurashi;
	}

	public boolean getIsUsingTyakuti() {
		return this.isUsingTyakuti;
	}

	public int getTrapCount() {
		return this.trapCount;
	}

	public Location getPlayerGroundLocation() {
		return this.playerGroundLocation;
	}

	public boolean getIsUsingSS() {
		return this.isUsingSS;
	}

	public Vector getVehicleVector() {
		return this.vehicleVector;
	}

	public boolean getIsJumping() {
		return this.isJumping;
	}

	public boolean getCanFly() {
		return this.isCanFly;
	}

	public double getSpeed() {
		return this.speed;
	}

	public float getFov() {
		return fov;
	}
	public ArmorStand getArmorlist(int n) {
		return this.Armorlist.get(n);
	}
	public Player getLastAttack() {
		return this.lastAttack;
	}
	public boolean getStoprun() {
		return this.stoprun;
	}

	public void setSettings(PlayerSettings settings) {
		this.settings = settings;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public void setIsInMatch(boolean isinmatch) {
		this.inmatch = isinmatch;
	}

	public void setWeaponClass(WeaponClass weaponclass) {
		this.weaponclass = weaponclass;
	}

	// public void setMainWeapon(MainWeapon mainweapon){this.mainweapon =
	// mainweapon;}

	public void setMatchLocation(Location l) {
		this.matchloc = l;
	}

	public void setPlayerNumber(int i) {
		this.playernumber = i;
	}

	public void setTick(int i) {
		this.tick = i;
	}

	public void setCanShoot(boolean canshoot) {
		this.canshoot = canshoot;
	}

	public void setIsSquid(boolean is) {
		this.issquid = is;
	}

	public void setIsOnInk(boolean is) {
		this.isonink = is;
	}

	public void setIsHolding(boolean is) {
		this.isHolding = is;
	}

	public void setIsJoined(boolean is) {
		this.isjoined = is;
	}

	public void setCanPaint(boolean is) {
		this.canpaint = is;
	}

	public void setArmor(double armor) {
		this.armor = armor;
	}

	public void setSPGauge(int spgauge) {
		this.spgauge = spgauge;
	}

	public void setIsBombRush(boolean is) {
		this.bombrush = is;
	}

	public void setIsSP(boolean is) {
		this.sp = is;
	}

	public void setCanUseSubWeapon(boolean is) {
		this.canusesub = is;
	}

	public void setIsCharging(boolean is) {
		this.charge = is;
	}

	public void setPoison(boolean is) {
		this.poison = is;
	}

	public void setCanRollerShoot(boolean is) {
		this.canrollershoot = is;
	}

	public void setPlayerMapLoc(Location loc) {
		this.maploc = loc;
	}

	public void setIsUsingSP(boolean is) {
		this.isusingsp = is;
	}

	public void setCanCharge(boolean is) {
		this.cancharge = is;
	}

	public void setIsDead(boolean is) {
		this.isdead = is;
	}

	public void setServerName(String server) {
		this.server = server;
	}

	public void setPlayerHead(ItemStack is) {
		this.is = is;
	}

	public void setIsOnPath(boolean is) {
		this.isonpath = is;
	}

	public void setIsUsingMM(boolean is) {
		this.isUsingMM = is;
	}

	public void setIsPoisonCoolTime(boolean is) {
		this.isPoisonCoolTime = is;
	}

	public void setIsSneaking(boolean is) {
		this.isSneak = is;
	}

	public void setIsUsingManeuver(boolean is) {
		this.isUsingManeuver = is;
	}

	public void setIsSliding(boolean is) {
		this.issliding = is;
	}

	public void setGearNumber(int i) {
		this.gearNumber = i;
	}

	public void setIsUsingJetPack(boolean is) {
		this.isUsingJetPack = is;
	}

	public void setMainItemGlow(boolean is) {
		this.MItemGlow = is;
	}

	public void setIsUsingAmehurashi(boolean is) {
		this.amehurashi = is;
	}

	public void setIsUsingTyakuti(boolean is) {
		this.isUsingTyakuti = is;
	}

	public void setPlayerGroundLocation(Location loc) {
		this.playerGroundLocation = loc;
	}

	public void setIsUsingSS(boolean is) {
		this.isUsingSS = is;
	}

	public void setVehicleVector(Vector vec) {
		this.vehicleVector = vec;
	}

	public void setIsJumping(boolean is) {
		this.isJumping = is;
	}

	public void setCanFly(boolean is) {
		this.isCanFly = is;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}
	public void setArmorlist(ArmorStand n) {
		this.Armorlist.add(n);
	}
	public void subArmorlist(ArmorStand n) {
		this.Armorlist.remove(n);
	}
	public void setLastAttack(Player p) {
		this.lastAttack = p;
	}
	public void setStoprun(boolean b) {
		this.stoprun = b;
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
