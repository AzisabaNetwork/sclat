package net.azisaba.sclat.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KProperty

/**
 * Example:
 * ```kotlin
 * val logger by DelegatedLogger()
 * ```
 */
class DelegatedLogger {
    private var logger: Logger? = null

    // TestClass2, KProperty1<*, *>
    operator fun <T : Any> getValue(
        thisRef: T,
        property: KProperty<*>,
    ): Logger =
        logger ?: run {
            val className =
                thisRef::class.java.name
                    .removeSuffix("\$Companion")
                    .split(".")
                    .last()
            LoggerFactory.getLogger("$PLUGIN_NAME:$className").also { logger = it }
        }
}
