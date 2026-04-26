package net.azisaba.sclat.core.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HostAndPortConfig(
    @SerialName("Host") val host: String,
    @SerialName("Port") val port: Int,
)

@Serializable
data class LocationConfig(
    @SerialName("WorldName") val worldName: String,
    @SerialName("X") val x: Double,
    @SerialName("Y") val y: Double,
    @SerialName("Z") val z: Double,
    @SerialName("Yaw") val yaw: Int = 0,
    @SerialName("Pitch") val pitch: Float = 0.0f,
)

@Serializable
data class TrainConfig(
    @SerialName("LFrom") val lFrom: LocationConfig? = null,
    @SerialName("LTo") val lTo: LocationConfig? = null,
    @SerialName("RFrom") val rFrom: LocationConfig? = null,
    @SerialName("RTo") val rTo: LocationConfig? = null,
)

// @SerialName("")
@Serializable
data class SclatConfig(
    val deferredMapLoading: Boolean = true,
    @SerialName("Tutorial") val tutorial: LocationConfig? = null,
    @SerialName("Colors") val colors: List<String> = listOf(),
    @SerialName("ParticlesRenderDistance") val particlesRenderDistance: Double = 0.0,
    @SerialName("WorkMode") val workMode: String,
    @SerialName("ServerType") val serverType: String,
    @SerialName("Shop") val shop: Boolean = true,
    @SerialName("BlockUpdateRate") val blockUpdateRate: Int,
    @SerialName("ResourcePackURL") val resourcePackUrl: String,
    @SerialName("DefaultClass") val defaultClass: String,
    @SerialName("EquipShare") val equipShare: Map<String, HostAndPortConfig> = mapOf(),
    @SerialName("StatusShare") val statusShare: HostAndPortConfig? = null,
    @SerialName("RateMatch") val rateMatch: Boolean? = null,
    @SerialName("RestartMatchCount") val restartMatchCount: Int? = null,
    @SerialName("MaxPlayerCount") val maxPlayerCount: Int,
    @SerialName("CanVoting") val canVoting: Boolean,
    @SerialName("StartPlayerCount") val startPlayerCount: Int,
    @SerialName("nBGM") val nBGM: Map<String, String> = mapOf(),
    @SerialName("fBGM") val fBGM: Map<String, String> = mapOf(),
    @SerialName("LobbyJump") val lobbyJump: LocationConfig? = null,
    @SerialName("TutorialClear") val tutorialClear: LocationConfig? = null,
    @SerialName("InkResetPeriod") val inkResetPeriod: Int = 20,
    @SerialName("Train") val train: TrainConfig? = null,
    @SerialName("WeaponRemove") val weaponRemove: LocationConfig? = null,
    // Hologram configs
    @SerialName("Hologram") val hologram: LocationConfig,
    @SerialName("HologramUpdatePeriod") val hologramUpdatePeriod: Long = 20L,
    @SerialName("RankingHolograms") val rankingHolograms: LocationConfig,
    @SerialName("MakeRankingPeriod") val makeRankingPeriod: Long = 20L,
    // Squid settings
    @SerialName("NormalRecovery") val normalRecovery: Double,
    @SerialName("SquidRecovery") val squidRecovery: Double,
    @SerialName("SquidSpeed") val squidSpeed: Double,
    @SerialName("PlayerWalkSpeed") val playerWalkSpeed: Float = 0.2f,
)
