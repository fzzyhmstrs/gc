package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.SyncedConfigHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

/**
 * Register a [SyncedConfigHelper.SyncedConfig] here. Syncd configs will automatically syncronize config data between the clients and server. See KDoc for the SyncedConfig for instructions on setting one up.
 */

object SyncedConfigRegistry {

    private val SYNC_CONFIG_PACKET = Identifier(AC.MOD_ID,"sync_config_packet")
    private val configs : MutableMap<String,SyncedConfigHelper.SyncedConfig> = mutableMapOf()

    internal fun registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_CONFIG_PACKET) { _, _, buf, _ ->
            val id = buf.readString()
            if (configs.containsKey(id)){
                configs[id]?.readFromServer(buf)
            }
        }
    }

    internal fun registerServer() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            configs.forEach {
                val buf = PacketByteBufs.create()
                buf.writeString(it.key)
                it.value.writeToClient(buf)
                ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf)
            }
        }
    }

    /**
     * register your config with this.
     *
     * Recommended implementation is to call this method within the overridden initConfig() method of [SyncedConfigHelper.SyncedConfig]
     *
     * initConfig() must then be called in your ModIntializer in order to complete the registration.
     *
     * [id] is a unique identifier for your config. The Mod ID is a typical choice.
     */
    fun registerConfig(id: String,config: SyncedConfigHelper.SyncedConfig){
        configs[id] = config
    }
}