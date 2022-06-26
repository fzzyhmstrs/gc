package me.fzzyhmstrs.amethyst_core

import me.fzzyhmstrs.amethyst_core.registry.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitialize() {
        LootRegistry.registerAll()
        RegisterBaseEntity.registerAll()
        EventRegistry.registerAll()
    }
}

object ACC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        RegisterBaseRenderer.registerAll()
        ItemModelRegistry.registerAll()
        EventRegistry.registerClient()
    }
}