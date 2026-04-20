package net.azisaba.sclat.core.extension

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

val toml =
    Toml(
        inputConfig =
            TomlInputConfig(
                ignoreUnknownNames = true,
                allowEmptyValues = true,
                allowNullValues = true,
                allowEscapedQuotesInLiteralStrings = true,
                allowEmptyToml = true,
                ignoreDefaultValues = false,
            ),
        outputConfig =
            TomlOutputConfig(
                indentation = TomlIndentation.NONE,
            ),
    )

inline fun <reified T> loadToml(file: File): T = toml.decodeFromString<T>(file.readText())

inline fun <reified T> saveToml(
    file: File,
    value: T,
) = file.writeText(toml.encodeToString(value))
