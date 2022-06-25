package me.fzzyhmstrs.amethyst_core.registry

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.misc_util.SyncedConfigHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

object SyncedConfigPacketRegistry {

    private val SYNC_CONFIG_PACKET = Identifier(AC.MOD_ID,"sync_config_packet")
    private val configs : MutableList<SyncedConfigHelper.SyncedConfig> = mutableListOf()

    internal fun registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_CONFIG_PACKET) { _, _, buf, _ ->
            configs.forEach {
                it.readFromServer(buf)
            }
        }
    }

    internal fun registerServer() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            configs.forEach {
                val buf = PacketByteBufs.create()
                it.writeToClient(buf)
                ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf)
            }
        }
    }

    fun registerConfig(config: SyncedConfigHelper.SyncedConfig){
        configs.add(config)
    }
}