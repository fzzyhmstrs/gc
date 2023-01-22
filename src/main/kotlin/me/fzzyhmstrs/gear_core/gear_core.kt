package me.fzzyhmstrs.gear_core

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint


object GC: ModInitializer {
    const val MOD_ID = "gear_core"

    override fun onInitialize() {
    }
}

object GCPreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {
        MixinExtrasBootstrap.init()
    }


}