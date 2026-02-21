package be4rjp.sclat.api.multiverse

object MultiverseApi {
    fun loadWorld(worldName: String): Boolean = multiverseCore.mvWorldManager.loadWorld(worldName)

    fun unloadWorld(
        worldName: String,
        save: Boolean = false,
    ): Boolean = multiverseCore.mvWorldManager.unloadWorld(worldName, save)
}
