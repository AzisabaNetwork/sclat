package be4rjp.sclat.data

import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.api.team.TeamLoc
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Be4rJP
 */
object DataMgr {
    @JvmStatic
    val playerDataMap: MutableMap<Player?, PlayerData?> = ConcurrentHashMap<Player?, PlayerData?>()

    @JvmStatic
    val uUIDDataMap: MutableMap<String?, PlayerData?> = HashMap<String?, PlayerData?>()
    private val matchdata: MutableMap<Int?, Match?> = HashMap<Int?, Match?>()
    private val teamdata: MutableMap<Int?, Team?> = HashMap<Int?, Team?>()
    private val colordata: MutableMap<String?, Color?> = HashMap<String?, Color?>()
    private val weaponclassdata: MutableMap<String?, WeaponClass?> = HashMap<String?, WeaponClass?>()
    private val weapondata: MutableMap<String?, MainWeapon?> = HashMap<String?, MainWeapon?>()
    private val mapdata: MutableMap<String?, MapData?> = HashMap<String?, MapData?>()
    private val locdata: MutableMap<MapData?, TeamLoc?> = HashMap<MapData?, TeamLoc?>()

    // public static void setPaintDataFromMatch(Match match, PaintData
    // data){paintdata.put(match, data);}
    @JvmStatic
    val blockDataMap: MutableMap<Block?, PaintData?> = HashMap<Block?, PaintData?>()

    @JvmStatic
    val playerIsQuitMap: MutableMap<String?, Boolean?> = HashMap<String?, Boolean?>()

    @JvmStatic
    val armorStandMap: MutableMap<ArmorStand?, Player?> = HashMap<ArmorStand?, Player?>()

    @JvmStatic
    val beaconMap: MutableMap<Player?, ArmorStand?> = HashMap<Player?, ArmorStand?>()

    @JvmStatic
    val sprinklerMap: MutableMap<Player?, ArmorStand?> = HashMap<Player?, ArmorStand?>()

    @JvmStatic
    val snowballIsHitMap: MutableMap<Projectile?, Boolean?> = HashMap<Projectile?, Boolean?>()

    @JvmStatic
    val mainSnowballIsHitMap: MutableMap<Projectile?, Boolean?> = HashMap<Projectile?, Boolean?>()

    @JvmStatic
    val spongeMap: MutableMap<Block?, Sponge?> = HashMap<Block?, Sponge?>()

    @JvmStatic
    val snowballNameMap: MutableMap<String?, Snowball?> = HashMap<String?, Snowball?>()

    @JvmStatic
    val mainSnowballNameMap: MutableMap<String?, Snowball?> = HashMap<String?, Snowball?>()
    private val msbn: MutableMap<String?, Int?> = HashMap<String?, Int?>()

    @JvmStatic
    val splashShieldDataMapWithPlayer: MutableMap<Player?, SplashShieldData?> = HashMap<Player?, SplashShieldData?>()

    @JvmStatic
    val splashShieldDataMapWithArmorStand: MutableMap<ArmorStand?, SplashShieldData?> =
        HashMap<ArmorStand?, SplashShieldData?>()

    @JvmStatic
    val kasaDataMapWithPlayer: MutableMap<Player?, KasaData?> = HashMap<Player?, KasaData?>()

    @JvmStatic
    val kAsaDataMapWithArmorStand: MutableMap<ArmorStand?, KasaData?> = HashMap<ArmorStand?, KasaData?>()

    // private static Map<Match, PaintData> paintdata = new HashMap<>();
    private val list: MutableList<Color> = ArrayList<Color>()

    @JvmField
    var oto: MutableMap<String?, Int?> = HashMap<String?, Int?>()
    var rblist: MutableList<Block?> = ArrayList<Block?>()

    @JvmField
    var al: MutableList<ArmorStand?> = ArrayList<ArmorStand?>()

    @JvmField
    var ssa: MutableList<ArmorStand?> = ArrayList<ArmorStand?>()

    @JvmField
    var mws: MutableList<String?> = ArrayList<String?>()

    @JvmField
    var pul: MutableList<String?> = ArrayList<String?>()

    @JvmField
    var tsl: MutableList<String?> = ArrayList<String?>()

    @JvmField
    var maplist: MutableList<MapData> = ArrayList<MapData>()
    var uuids: MutableList<String?> = ArrayList<String?>()

    @JvmField
    var joinedList: MutableList<Player?> = ArrayList<Player?>()

    @JvmStatic
    fun getPlayerData(player: Player?): PlayerData? = playerDataMap.get(player)

    fun getUUIDData(uuid: String?): PlayerData? = uUIDDataMap.get(uuid)

    @JvmStatic
    fun getMatchFromId(id: Int): Match? = matchdata.get(id)

    fun getTeamFromId(id: Int): Team? = teamdata.get(id)

    fun getColor(name: String?): Color? = colordata.get(name)

    @JvmStatic
    fun getWeaponClass(weaponclass: String?): WeaponClass? = weaponclassdata.get(weaponclass)

    @JvmStatic
    fun getWeapon(name: String?): MainWeapon? = weapondata.get(name)

    fun getMap(name: String?): MapData? = mapdata.get(name)

    fun getTeamLoc(map: MapData?): TeamLoc? = locdata.get(map)

    @JvmStatic
    fun getPaintDataFromBlock(block: Block?): PaintData? = blockDataMap.get(block)

    @JvmStatic
    fun getPlayerIsQuit(uuid: String?): Boolean = playerIsQuitMap.get(uuid)!!

    @JvmStatic
    fun getArmorStandPlayer(`as`: ArmorStand?): Player? = armorStandMap.get(`as`)

    @JvmStatic
    fun getSnowballIsHit(ball: Projectile?): Boolean = snowballIsHitMap.get(ball)!!

    fun getMainSnowballIsHit(ball: Projectile?): Boolean = mainSnowballIsHitMap.get(ball)!!

    @JvmStatic
    fun getBeaconFromplayer(player: Player?): ArmorStand? = beaconMap.get(player)

    @JvmStatic
    fun getSprinklerFromplayer(player: Player?): ArmorStand? = sprinklerMap.get(player)

    @JvmStatic
    fun getSpongeFromBlock(block: Block?): Sponge? = spongeMap.get(block)

    fun getSplashShieldDataFromPlayer(player: Player?): SplashShieldData? = splashShieldDataMapWithPlayer.get(player)

    @JvmStatic
    fun getSplashShieldDataFromArmorStand(`as`: ArmorStand?): SplashShieldData? = splashShieldDataMapWithArmorStand.get(`as`)

    fun getKasaDataFromPlayer(player: Player?): KasaData? = kasaDataMapWithPlayer.get(player)

    @JvmStatic
    fun getKasaDataFromArmorStand(`as`: ArmorStand?): KasaData? = kAsaDataMapWithArmorStand.get(`as`)

    @JvmStatic
    fun getSnowballHitCount(name: String?): Int = msbn.get(name)!!

    // public static PaintData getPaintDataFromMatch(Match match){return
    // paintdata.get(match);}
    @JvmStatic
    fun setPlayerData(
        player: Player?,
        data: PlayerData?,
    ) {
        playerDataMap.put(player, data)
    }

    @JvmStatic
    fun setUUIDData(
        uuid: String?,
        data: PlayerData?,
    ) {
        uUIDDataMap.put(uuid, data)
    }

    @JvmStatic
    fun setMatch(
        id: Int,
        match: Match?,
    ) {
        matchdata.put(id, match)
    }

    @JvmStatic
    fun setColor(
        name: String?,
        color: Color?,
    ) {
        colordata.put(name, color)
    }

    @JvmStatic
    fun setTeam(
        id: Int,
        team: Team?,
    ) {
        teamdata.put(id, team)
    }

    @JvmStatic
    fun setWeaponClass(
        WCname: String?,
        weaponclass: WeaponClass?,
    ) {
        weaponclassdata.put(WCname, weaponclass)
    }

    @JvmStatic
    fun setMainWeapon(
        MWname: String?,
        mw: MainWeapon?,
    ) {
        weapondata.put(MWname, mw)
    }

    fun setMap(
        Mname: String?,
        map: MapData?,
    ) {
        mapdata.put(Mname, map)
    }

    fun setTeamLoc(
        map: MapData?,
        loc: TeamLoc?,
    ) {
        locdata.put(map, loc)
    }

    @JvmStatic
    fun setPaintDataFromBlock(
        block: Block?,
        data: PaintData?,
    ) {
        blockDataMap.put(block, data)
    }

    @JvmStatic
    fun setPlayerIsQuit(
        uuid: String?,
        `is`: Boolean,
    ) {
        playerIsQuitMap.put(uuid, `is`)
    }

    @JvmStatic
    fun setArmorStandPlayer(
        `as`: ArmorStand?,
        player: Player?,
    ) {
        armorStandMap.put(`as`, player)
    }

    @JvmStatic
    fun setSnowballIsHit(
        ball: Projectile?,
        `is`: Boolean,
    ) {
        snowballIsHitMap.put(ball, `is`)
    }

    fun setMainSnowballIsHit(
        ball: Projectile?,
        `is`: Boolean,
    ) {
        mainSnowballIsHitMap.put(ball, `is`)
    }

    @JvmStatic
    fun setBeaconFromPlayer(
        player: Player?,
        `as`: ArmorStand?,
    ) {
        beaconMap.put(player, `as`)
    }

    @JvmStatic
    fun setSprinklerFromPlayer(
        player: Player?,
        `as`: ArmorStand?,
    ) {
        sprinklerMap.put(player, `as`)
    }

    @JvmStatic
    fun setSpongeWithBlock(
        block: Block?,
        sponge: Sponge?,
    ) {
        spongeMap.put(block, sponge)
    }

    @JvmStatic
    fun setSplashShieldDataWithPlayer(
        player: Player?,
        data: SplashShieldData?,
    ) {
        splashShieldDataMapWithPlayer.put(player, data)
    }

    @JvmStatic
    fun setSplashShieldDataWithARmorStand(
        `as`: ArmorStand?,
        data: SplashShieldData?,
    ) {
        splashShieldDataMapWithArmorStand.put(`as`, data)
    }

    @JvmStatic
    fun setKasaDataWithPlayer(
        player: Player?,
        data: KasaData?,
    ) {
        kasaDataMapWithPlayer.put(player, data)
    }

    @JvmStatic
    fun setKasaDataWithARmorStand(
        `as`: ArmorStand?,
        data: KasaData?,
    ) {
        kAsaDataMapWithArmorStand.put(`as`, data)
    }

    @JvmStatic
    fun setSnowballHitCount(
        name: String?,
        coount: Int,
    ) {
        msbn.putIfAbsent(name, coount)
    }

    @JvmStatic
    fun addColorList(color: Color?) {
        list.add(color!!)
    }

    @JvmStatic
    fun addPathArmorStandList(`as`: ArmorStand?) {
        al.add(`as`)
    }

    @JvmStatic
    fun addMapList(map: MapData?) {
        maplist.add(map!!)
    }

    @JvmStatic
    fun addSnowballHitCount(name: String?) {
        msbn.put(name, getSnowballHitCount(name) + 1)
    }

    // public static Map<Match, PaintData> getPaintDataMap(){return paintdata;}
    @JvmStatic
    fun getColorRandom(number: Int): Color {
        val color = list.get(number)
        color.isUsed = true
        return color // RandomColor
    }

    @JvmStatic
    fun ColorShuffle() {
        Collections.shuffle(list)
    }

    @JvmStatic
    fun MapDataShuffle() {
        Collections.shuffle(maplist)
    }

    @JvmStatic
    fun getMapRandom(i: Int): MapData? {
        val map = maplist.get(i)
        return map // RandomMap
    }
}
