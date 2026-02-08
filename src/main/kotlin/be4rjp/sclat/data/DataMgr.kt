package be4rjp.sclat.data

import be4rjp.sclat.api.player.PlayerData
import be4rjp.sclat.api.team.Team
import be4rjp.sclat.api.team.TeamLoc
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Be4rJP
 */
object DataMgr {
    @JvmStatic
    val playerDataMap: MutableMap<Player, PlayerData> = ConcurrentHashMap()

    @JvmStatic
    val uUIDDataMap: MutableMap<String?, PlayerData?> = HashMap()
    private val matchdata: MutableMap<Int?, Match?> = HashMap()
    private val teamdata: MutableMap<Int?, Team?> = HashMap()
    private val colordata: MutableMap<String?, Color?> = HashMap()
    private val weaponclassdata: MutableMap<String?, WeaponClass?> = HashMap()
    private val weapondata: MutableMap<String?, MainWeapon?> = HashMap()
    private val mapdata: MutableMap<String?, MapData?> = HashMap()
    private val locdata: MutableMap<MapData?, TeamLoc?> = HashMap()

    // public static void setPaintDataFromMatch(Match match, PaintData
    // data){paintdata.put(match, data);}
    @JvmStatic
    val blockDataMap: MutableMap<Block?, PaintData?> = HashMap()

    @JvmStatic
    val playerIsQuitMap: MutableMap<String?, Boolean?> = HashMap()

    @JvmStatic
    val armorStandMap: MutableMap<ArmorStand?, Player?> = HashMap()

    @JvmStatic
    val beaconMap: MutableMap<Player?, ArmorStand?> = HashMap()

    @JvmStatic
    val sprinklerMap: MutableMap<Player?, ArmorStand?> = HashMap()

    @JvmStatic
    val snowballIsHitMap: MutableMap<Projectile?, Boolean?> = HashMap()

    @JvmStatic
    val mainSnowballIsHitMap: MutableMap<Projectile?, Boolean?> = HashMap()

    @JvmStatic
    val spongeMap: MutableMap<Block?, Sponge?> = HashMap()

    @JvmStatic
    val snowballNameMap: MutableMap<String?, Snowball?> = HashMap()

    @JvmStatic
    val mainSnowballNameMap: MutableMap<String?, Snowball?> = HashMap()
    private val msbn: MutableMap<String?, Int?> = HashMap()

    @JvmStatic
    val splashShieldDataMapWithPlayer: MutableMap<Player?, SplashShieldData?> = HashMap()

    @JvmStatic
    val splashShieldDataMapWithArmorStand: MutableMap<ArmorStand?, SplashShieldData?> =
        HashMap()

    @JvmStatic
    val kasaDataMapWithPlayer: MutableMap<Player?, KasaData?> = HashMap()

    @JvmStatic
    val kAsaDataMapWithArmorStand: MutableMap<ArmorStand?, KasaData?> = HashMap()

    // private static Map<Match, PaintData> paintdata = new HashMap<>();
    private val list: MutableList<Color> = ArrayList()

    @JvmField
    var oto: MutableMap<String?, Int?> = HashMap()
    var rblist: MutableList<Block?> = ArrayList()

    @JvmField
    var al: MutableList<ArmorStand?> = ArrayList()

    @JvmField
    var ssa: MutableList<ArmorStand?> = ArrayList()

    @JvmField
    var mws: MutableList<String?> = ArrayList()

    @JvmField
    var pul: MutableList<String?> = ArrayList()

    @JvmField
    var tsl: MutableList<String?> = ArrayList()

    @JvmField
    var maplist: MutableList<MapData> = ArrayList()
    var uuids: MutableList<String?> = ArrayList()

    @JvmField
    var joinedList: MutableList<Player?> = ArrayList()

    @JvmStatic
    fun getPlayerData(player: Player?): PlayerData? = playerDataMap[player]

    fun getUUIDData(uuid: String?): PlayerData? = uUIDDataMap[uuid]

    @JvmStatic
    fun getMatchFromId(id: Int): Match? = matchdata[id]

    fun getTeamFromId(id: Int): Team? = teamdata[id]

    fun getColor(name: String?): Color? = colordata[name]

    @JvmStatic
    fun getWeaponClass(weaponclass: String?): WeaponClass? = weaponclassdata[weaponclass]

    @JvmStatic
    fun getWeapon(name: String?): MainWeapon? = weapondata[name]

    fun getMap(name: String?): MapData? = mapdata[name]

    fun getTeamLoc(map: MapData?): TeamLoc? = locdata[map]

    @JvmStatic
    fun getPaintDataFromBlock(block: Block?): PaintData? = blockDataMap[block]

    @JvmStatic
    fun getPlayerIsQuit(uuid: String?): Boolean = playerIsQuitMap[uuid]!!

    @JvmStatic
    fun getArmorStandPlayer(`as`: ArmorStand?): Player? = armorStandMap[`as`]

    @JvmStatic
    fun getSnowballIsHit(ball: Projectile?): Boolean = snowballIsHitMap[ball]!!

    fun getMainSnowballIsHit(ball: Projectile?): Boolean = mainSnowballIsHitMap[ball]!!

    @JvmStatic
    fun getBeaconFromplayer(player: Player?): ArmorStand? = beaconMap[player]

    @JvmStatic
    fun getSprinklerFromplayer(player: Player?): ArmorStand? = sprinklerMap[player]

    @JvmStatic
    fun getSpongeFromBlock(block: Block?): Sponge? = spongeMap[block]

    fun getSplashShieldDataFromPlayer(player: Player?): SplashShieldData? = splashShieldDataMapWithPlayer[player]

    @JvmStatic
    fun getSplashShieldDataFromArmorStand(`as`: ArmorStand?): SplashShieldData? = splashShieldDataMapWithArmorStand[`as`]

    fun getKasaDataFromPlayer(player: Player?): KasaData? = kasaDataMapWithPlayer[player]

    @JvmStatic
    fun getKasaDataFromArmorStand(`as`: ArmorStand?): KasaData? = kAsaDataMapWithArmorStand[`as`]

    @JvmStatic
    fun getSnowballHitCount(name: String?): Int = msbn[name]!!

    // public static PaintData getPaintDataFromMatch(Match match){return
    // paintdata.get(match);}
    @JvmStatic
    fun setPlayerData(
        player: Player,
        data: PlayerData,
    ) {
        playerDataMap[player] = data
    }

    @JvmStatic
    fun setUUIDData(
        uuid: String?,
        data: PlayerData?,
    ) {
        uUIDDataMap[uuid] = data
    }

    @JvmStatic
    fun setMatch(
        id: Int,
        match: Match?,
    ) {
        matchdata[id] = match
    }

    @JvmStatic
    fun setColor(
        name: String?,
        color: Color?,
    ) {
        colordata[name] = color
    }

    @JvmStatic
    fun setTeam(
        id: Int,
        team: Team?,
    ) {
        teamdata[id] = team
    }

    @JvmStatic
    fun setWeaponClass(
        WCname: String?,
        weaponclass: WeaponClass?,
    ) {
        weaponclassdata[WCname] = weaponclass
    }

    @JvmStatic
    fun setMainWeapon(
        MWname: String?,
        mw: MainWeapon?,
    ) {
        weapondata[MWname] = mw
    }

    fun setMap(
        Mname: String?,
        map: MapData?,
    ) {
        mapdata[Mname] = map
    }

    fun setTeamLoc(
        map: MapData?,
        loc: TeamLoc?,
    ) {
        locdata[map] = loc
    }

    @JvmStatic
    fun setPaintDataFromBlock(
        block: Block?,
        data: PaintData?,
    ) {
        blockDataMap[block] = data
    }

    @JvmStatic
    fun setPlayerIsQuit(
        uuid: String?,
        `is`: Boolean,
    ) {
        playerIsQuitMap[uuid] = `is`
    }

    @JvmStatic
    fun setArmorStandPlayer(
        `as`: ArmorStand?,
        player: Player?,
    ) {
        armorStandMap[`as`] = player
    }

    @JvmStatic
    fun setSnowballIsHit(
        ball: Projectile?,
        `is`: Boolean,
    ) {
        snowballIsHitMap[ball] = `is`
    }

    fun setMainSnowballIsHit(
        ball: Projectile?,
        `is`: Boolean,
    ) {
        mainSnowballIsHitMap[ball] = `is`
    }

    @JvmStatic
    fun setBeaconFromPlayer(
        player: Player?,
        `as`: ArmorStand?,
    ) {
        beaconMap[player] = `as`
    }

    @JvmStatic
    fun setSprinklerFromPlayer(
        player: Player?,
        `as`: ArmorStand?,
    ) {
        sprinklerMap[player] = `as`
    }

    @JvmStatic
    fun setSpongeWithBlock(
        block: Block?,
        sponge: Sponge?,
    ) {
        spongeMap[block] = sponge
    }

    @JvmStatic
    fun setSplashShieldDataWithPlayer(
        player: Player?,
        data: SplashShieldData?,
    ) {
        splashShieldDataMapWithPlayer[player] = data
    }

    @JvmStatic
    fun setSplashShieldDataWithARmorStand(
        `as`: ArmorStand?,
        data: SplashShieldData?,
    ) {
        splashShieldDataMapWithArmorStand[`as`] = data
    }

    @JvmStatic
    fun setKasaDataWithPlayer(
        player: Player?,
        data: KasaData?,
    ) {
        kasaDataMapWithPlayer[player] = data
    }

    @JvmStatic
    fun setKasaDataWithARmorStand(
        `as`: ArmorStand?,
        data: KasaData?,
    ) {
        kAsaDataMapWithArmorStand[`as`] = data
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
        msbn[name] = getSnowballHitCount(name) + 1
    }

    // public static Map<Match, PaintData> getPaintDataMap(){return paintdata;}
    @JvmStatic
    fun getColorRandom(number: Int): Color {
        val color = list[number]
        color.isUsed = true
        return color // RandomColor
    }

    @JvmStatic
    fun colorShuffle() {
        list.shuffle()
    }

    @JvmStatic
    fun mapDataShuffle() {
        maplist.shuffle()
    }

    @JvmStatic
    fun getMapRandom(i: Int): MapData {
        val map = maplist[i]
        return map // RandomMap
    }
}
