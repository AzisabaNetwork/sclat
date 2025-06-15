package be4rjp.sclat.data;

import org.bukkit.inventory.ItemStack;

/**
 * @author Be4rJP
 */
public class MainWeapon {
    private final String WeaponName;
    private String WeaponType;
    private ItemStack weaponitem;
    private double random;
    private double maxRandom = 0;
    private int distancetick;
    private double shootspeed;
    private int shoottick;
    private int paintrandom;
    private double maxpaintdis;
    private float needink;
    private double damage;
    private int maxcharge;
    private int rollershootquantity;
    private float UsingWalkSpeed;
    private int rollerWidth;
    private boolean Tatehuri;
    private double rollerdamage;
    private float rollerneedink;
    private boolean scope;
    private double exh;
    private int delay;
    private int cooltime;
    private double exd;
    private boolean hude;
    private double huder;
    private int money;
    private boolean isManeuver = false;
    private int slidingshoottick = 0;
    private double chargeratio = 1.0;
    private float InHoldSpeed = 0.2F;
    private boolean canChargeKeep = false;
    private int chargeKeepingTime = 0;
    private boolean hanbunCharge = false;
    private double SPRate = 1.0;
    private int maxRandomCount = 1;
    private int level = 0;
    private float slideNeedINK = 0.2F;
    private double DecreaseRate = 1.0;//チャージャーの非適性射程でのダメージ減少率
    private int AppDistance = 0;//非適性射程の距離
    private boolean islootbox = false;//ガチャ武器かどうか

    private double lootpro = 0;//ガチャ排出率


    //スワッパー
    private String swap = "";
    private boolean isswaper = false;

    public MainWeapon(String weaponname) {
        this.WeaponName = weaponname;
    }

    public String getWeaponType() {
        return this.WeaponType;
    }

    public void setWeaponType(String WT) {
        this.WeaponType = WT;
    }

    public ItemStack getWeaponIteamStack() {
        return this.weaponitem;
    }

    public double getRandom() {
        return random;
    }

    public void setRandom(double random) {
        this.random = random;
    }

    public double getMaxRandom() {
        return maxRandom;
    }

    public void setMaxRandom(double random) {
        this.maxRandom = random;
    }

    public int getDistanceTick() {
        return distancetick;
    }

    public void setDistanceTick(int distick) {
        this.distancetick = distick;
    }

    public double getShootSpeed() {
        return shootspeed;
    }

    public void setShootSpeed(double speed) {
        this.shootspeed = speed;
    }

    public int getShootTick() {
        return shoottick;
    }

    public void setShootTick(int shoottick) {
        this.shoottick = shoottick;
    }

    public int getPaintRandom() {
        return this.paintrandom;
    }

    public void setPaintRandom(int r) {
        this.paintrandom = r;
    }

    public double getMaxPaintDis() {
        return this.maxpaintdis;
    }

    public void setMaxPaintDis(double max) {
        this.maxpaintdis = max;
    }

    public float getNeedInk() {
        return this.needink;
    }

    public void setNeedInk(float ink) {
        this.needink = ink;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getMaxCharge() {
        return this.maxcharge;
    }

    public void setMaxCharge(int max) {
        this.maxcharge = max;
    }

    public int getRollerShootQuantity() {
        return this.rollershootquantity;
    }

    public void setRollerShootQuantity(int i) {
        this.rollershootquantity = i;
    }

    public float getUsingWalkSpeed() {
        return this.UsingWalkSpeed;
    }

    public void setUsingWalkSpeed(float f) {
        this.UsingWalkSpeed = f;
    }

    public int getRollerWidth() {
        return this.rollerWidth;
    }

    public void setRollerWidth(int w) {
        this.rollerWidth = w;
    }

    public boolean getCanTatehuri() {
        return this.Tatehuri;
    }

    public void setCanTatehuri(boolean t) {
        this.Tatehuri = t;
    }

    public double getRollerDamage() {
        return this.rollerdamage;
    }

    public void setRollerDamage(double damage) {
        this.rollerdamage = damage;
    }

    public float getRollerNeedInk() {
        return this.rollerneedink;
    }

    public void setRollerNeedInk(float ink) {
        this.rollerneedink = ink;
    }

    public boolean getScope() {
        return this.scope;
    }

    public void setScope(boolean is) {
        this.scope = is;
    }

    public double getBlasterExHankei() {
        return this.exh;
    }

    public void setBlasterExHankei(double d) {
        this.exh = d;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setDelay(int i) {
        this.delay = i;
    }

    public int getCoolTime() {
        return this.cooltime;
    }

    public void setCoolTime(int i) {
        this.cooltime = i;
    }

    public double getBlasterExDamage() {
        return this.exd;
    }

    public void setBlasterExDamage(double d) {
        this.exd = d;
    }

    public boolean getIsHude() {
        return this.hude;
    }

    public void setIsHude(boolean is) {
        this.hude = is;
    }

    public double getHudeRandom() {
        return this.huder;
    }

    public void setHudeRandom(double d) {
        this.huder = d;
    }

    public int getMoney() {
        return this.money;
    }

    public void setMoney(int i) {
        this.money = i;
    }

    public boolean getIsManeuver() {
        return this.isManeuver;
    }

    public void setIsManeuver(boolean is) {
        this.isManeuver = is;
    }

    public int getSlidingShootTick() {
        return this.slidingshoottick;
    }

    public void setSlidingShootTick(int i) {
        this.slidingshoottick = i;
    }

    public double getChargeRatio() {
        return this.chargeratio;
    }

    public void setChargeRatio(double r) {
        this.chargeratio = r;
    }

    public float getInHoldSpeed() {
        return this.InHoldSpeed;
    }

    public void setInHoldSpeed(float s) {
        this.InHoldSpeed = s;
    }

    public boolean getCanChargeKeep() {
        return this.canChargeKeep;
    }

    public void setCanChargeKeep(boolean is) {
        this.canChargeKeep = is;
    }

    public int getChargeKeepingTime() {
        return this.chargeKeepingTime;
    }

    public void setChargeKeepingTime(int i) {
        this.chargeKeepingTime = i;
    }

    public boolean getHanbunCharge() {
        return this.hanbunCharge;
    }

    public void setHanbunCharge(boolean is) {
        this.hanbunCharge = is;
    }

    public double getSPRate() {
        return this.SPRate;
    }

    public void setSPRate(double rate) {
        this.SPRate = rate;
    }

    public int getMaxRandomCount() {
        return this.maxRandomCount;
    }

    public void setMaxRandomCount(int count) {
        this.maxRandomCount = count;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getSlideNeedINK() {
        return this.slideNeedINK;
    }

    public void setSlideNeedINK(float ink) {
        this.slideNeedINK = ink;
    }

    public double getDecreaseRate() {
        return this.DecreaseRate;
    }//チャージャーの非適性射程でのダメージ減少率の取得

    public void setDecreaseRate(double srDecRate) {
        this.DecreaseRate = srDecRate;
    }//チャージャーの非適性射程でのダメージ減少率の設定

    public int getAppDistance() {
        return this.AppDistance;
    }//チャージャーの非適性射程の取得

    public void setAppDistance(int srAppDistance) {
        this.AppDistance = srAppDistance;
    }//チャージャーの非適性射程の設定

    public boolean getIslootbox() {
        return this.islootbox;
    }//ガチャ武器かどうかを返す

    public void setIslootbox(boolean isloot) {
        this.islootbox = isloot;
    }//ガチャ武器かどうかを返す

    public double getLootpro() {
        return this.lootpro;
    }//排出率を返す

    public void setLootpro(double pro) {
        this.lootpro = pro;
    }//排出率を返す

    public String getSwap() {
        return swap;
    }

    public void setSwap(String swapweapon) {
        this.swap = swapweapon;
    }

    public boolean getIsSwap() {
        return isswaper;
    }

    public void setIsSwap(boolean Isswap) {
        this.isswaper = Isswap;
    }

    public void setWeaponItemStack(ItemStack is) {
        this.weaponitem = is;
    }
}
