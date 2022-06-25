package me.fzzyhmstrs.amethyst_core

import net.fabricmc.api.ModInitializer
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitialize() {
    }
}