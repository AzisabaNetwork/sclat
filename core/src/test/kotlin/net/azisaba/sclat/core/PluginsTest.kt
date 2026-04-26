package net.azisaba.sclat.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.plugin.PluginManager

class PluginsTest :
    StringSpec({
        "all enabled" {
            val pluginManager = mockk<PluginManager>()
            every { pluginManager.isPluginEnabled("DADADAChecker") } returns true
            every { pluginManager.isPluginEnabled("LunaChat") } returns true
            every { pluginManager.isPluginEnabled("NoteBlockAPI") } returns true
            every { pluginManager.isPluginEnabled("ProtocolLib") } returns true
            Plugins.onInit(pluginManager) shouldBe true
        }

        "missing non-required" {
            val pluginManager = mockk<PluginManager>()
            every { pluginManager.isPluginEnabled("DADADAChecker") } returns true
            every { pluginManager.isPluginEnabled("LunaChat") } returns false
            every { pluginManager.isPluginEnabled("NoteBlockAPI") } returns true
            every { pluginManager.isPluginEnabled("ProtocolLib") } returns true
            Plugins.onInit(pluginManager) shouldBe true
        }

        "missing required" {
            val pluginManager = mockk<PluginManager>()
            every { pluginManager.isPluginEnabled("DADADAChecker") } returns false
            every { pluginManager.isPluginEnabled("LunaChat") } returns true
            every { pluginManager.isPluginEnabled("NoteBlockAPI") } returns true
            every { pluginManager.isPluginEnabled("ProtocolLib") } returns true
            Plugins.onInit(pluginManager) shouldBe false
        }

        "missing required many" {
            val pluginManager = mockk<PluginManager>()
            every { pluginManager.isPluginEnabled("DADADAChecker") } returns false
            every { pluginManager.isPluginEnabled("LunaChat") } returns true
            every { pluginManager.isPluginEnabled("NoteBlockAPI") } returns false
            every { pluginManager.isPluginEnabled("ProtocolLib") } returns true
            Plugins.onInit(pluginManager) shouldBe false
        }
    })
