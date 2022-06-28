package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.AC
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.function.Consumer

interface ParticleEmitting{
    
    fun emitParticles(world: World, client: MinecraftClient, user: LivingEntity)
    
    fun particleChance(): Int{
        return 1
    }
    
    fun sendParticlePacket(user: ServerPlayerEntity, id: String){
        val buf = PacketByteBufs.create()
        buf.writeString(id)
        ServerPlayNetworking.send(user, EMITTED_PARTICLE_PACKET, buf)
    }

    companion object{
        private val EMITTED_PARTICLE_PACKET = Identifier(AC.MOD_ID,"emitted_particle_packet")
        private val emitters: MutableMap<String, Consumer<MinecraftClient>> = mutableMapOf()
        
        fun registerParticleEmitter(id: String, emitter: Consumer<MinecraftClient>){
            emitters[id] = emitter
        }
        
        internal fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(EMITTED_PARTICLE_PACKET) { minecraftClient: MinecraftClient, _, buf, _ ->
                val id = buf.readString()
                emitters[id]?.accept(minecraftClient)
            }
        }
    
    }
    
}
