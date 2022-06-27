package me.fzzyhmstrs.amethyst_core.item_util

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
        private val emitters: mutableMap<String, Consumer<MinecraftClient>> = mutableMapOf()
        
        fun registerParticleEmitter(id: String, emitter: Consumer<MinecraftClient>){
            emitters[id] = emitter
        }
        
        internal fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(EMITTED_PARTICLE_PACKET) { minecraftClient: MinecraftClient, _, _, _ ->
                val id = buf.readString()
                emitter[id]?.accept(minecraftClient)
            }
        }
    
    }
    
}
