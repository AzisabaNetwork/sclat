package net.azisaba.sclat.core.config

import kotlinx.serialization.Serializable

@Serializable
data class LoginBonusRewardConfig(
    val money: Int = 5000,
    val ticket: Int = 50,
)
