package me.fzzyhmstrs.amethyst_core.config

import com.google.gson.GsonBuilder
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.SyncedConfigHelper
import me.fzzyhmstrs.amethyst_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.amethyst_core.registry.SyncedConfigRegistry
import net.minecraft.network.PacketByteBuf

object AcConfig: SyncedConfigHelper.SyncedConfig {


    var flavors: Flavors

    init{
        flavors = SyncedConfigHelper.readOrCreate("flavors_v0.json") { Flavors() }
        ReadmeText.writeReadMe("README.txt", AC.MOD_ID)
    }

    override fun readFromServer(buf: PacketByteBuf) {
        flavors = gson.fromJson(buf.readString(),Flavors::class.java)
    }

    override fun writeToClient(buf: PacketByteBuf) {
        val gson = GsonBuilder().create()
        buf.writeString(gson.toJson(flavors))
    }

    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(AC.MOD_ID,this)
    }

    class Flavors{
        var showFlavorDesc: Boolean = false
        var showFlavorDescOnAdvanced: Boolean = true
    }

}