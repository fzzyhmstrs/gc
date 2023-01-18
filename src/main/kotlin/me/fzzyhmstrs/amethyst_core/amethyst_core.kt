package me.fzzyhmstrs.amethyst_core

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import me.fzzyhmstrs.amethyst_core.config.AcConfig
import me.fzzyhmstrs.amethyst_core.registry.*
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.PlaceItemAugment
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.util.Identifier
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())
    val fallbackId = Identifier("vanishing_curse")

    override fun onInitialize() {
        AcConfig.initConfig()
        LootRegistry.registerAll()
        RegisterBaseEntity.registerAll()
        EventRegistry.registerAll()
        ModifierRegistry.registerAll()
    }
}

object ACC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        RegisterBaseRenderer.registerAll()
        ItemModelRegistry.registerAll()
        EventRegistry.registerClient()
        PlaceItemAugment.registerClient()
    }
}

object ACPreLaunch: PreLaunchEntrypoint{

    override fun onPreLaunch() {
        MixinExtrasBootstrap.init()
    }

}