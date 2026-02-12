package be4rjp.sclat.config

import kotlinx.serialization.Serializable

@Serializable
data class LoginBonusConfig(
    val money: Int = 5000,
    val ticket: Int = 50,
)
