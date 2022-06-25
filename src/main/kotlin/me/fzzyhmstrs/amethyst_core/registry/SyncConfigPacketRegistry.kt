package me.fzzyhmstrs.amethyst_core.registry

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.fzzyhmstrs.amethyst_core.AC
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.io.File

object SyncConfigPacketRegistry {

    private val SYNC_CONFIG_PACKET = Identifier(AC.MOD_ID,"sync_config_packet")
    private val configs : MutableList<SyncedConfig> = mutableListOf()
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

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

    fun registerConfig(config: SyncedConfig){
        configs.add(config)
    }

    inline fun <reified T> readOrCreate(file: String, child: String = "", base: String = AC.MOD_ID, configClass: () -> T): T {
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val f = File(dir, file)
        try {
            if (f.exists()) {
                return gson.fromJson(f.readLines().joinToString(""), T::class.java)
            } else if (!f.createNewFile()) {
                println("Failed to create default config file ($file), using default config.")
            } else {
                f.writeText(gson.toJson(configClass()))
            }
            return configClass()
        } catch (e: Exception) {
            println("Failed to read config file! Using default values: " + e.message)
            return configClass()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    inline fun <reified T, reified P> readOrCreateUpdated(file: String, previous: String, child: String = "", base: String = AC.MOD_ID, configClass: () -> T, previousClass: () -> P): T{
        val (dir,dirCreated) = makeDir(child, base)
        if (!dirCreated) {
            return configClass()
        }
        val p = File(dir, previous)
        try {
            if (p.exists()) {
                val previousConfig = gson.fromJson(p.readLines().joinToString(""), P::class.java)
                if (previousConfig is OldClass){
                    val newClass = previousConfig.generateNewClass()
                    if (newClass !is T){
                        throw RuntimeException("Old config class is not returning the proper new config class: ${P::class.simpleName} is returning ${newClass.javaClass.simpleName}; expected ${T::class.simpleName}")
                    } else {
                        val f = File(dir,file)
                        if (f.exists()){
                            p.delete() //attempts to delete the now useless old config version file
                            return gson.fromJson(f.readLines().joinToString(""), T::class.java)
                        } else if (!f.createNewFile()){
                            //don't delete old file if the new one can't be generated to take its place
                            println("Failed to create new config file ($file), using old config with new defaults.")
                        } else {
                            p.delete() //attempts to delete the now useless old config version file
                            f.writeText(gson.toJson(newClass))
                        }
                        return newClass
                    }
                } else {
                    throw RuntimeException("Old config not properly set up as an OldConfig: ${P::class.simpleName}")
                }
            } else {
                return readOrCreate(file, configClass = configClass)
            }
        } catch (e: Exception) {
            println("Failed to read config file! Using default values: " + e.message)
            return configClass()
        }
    }

    fun makeDir(child: String, base: String): Pair<File,Boolean>{
        val dir = if (child != ""){
            File(File(FabricLoader.getInstance().configDir.toFile(), base), child)
        } else {
            File(FabricLoader.getInstance().configDir.toFile(), base)
        }
        if (!dir.exists() && !dir.mkdirs()) {
            println("Could not create directory, using default configs.")
            return Pair(dir,false)
        }
        return Pair(dir,true)
    }

    interface SyncedConfig{
        fun readFromServer(buf: PacketByteBuf)
        fun writeToClient(buf: PacketByteBuf)
    }
    interface OldClass{

        fun generateNewClass(): Any

    }
}