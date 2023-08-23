package me.fzzyhmstrs.gear_core.set

import com.google.common.collect.HashMultimap
import com.google.gson.Gson
import com.google.gson.JsonParser
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil
import me.fzzyhmstrs.gear_core.GC
import me.fzzyhmstrs.gear_core.interfaces.ActiveGearSetsTracking
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object GearSets: SimpleSynchronousResourceReloadListener {

    private val gearSets: MutableMap<Identifier,GearSet> = mutableMapOf()
    private val cachedSets: HashMultimap<Item,GearSet> = HashMultimap.create()
    private val setsToSend: MutableMap<Identifier,String> = mutableMapOf()
    private val cachedSetsToSend: HashMultimap<Identifier,Identifier> = HashMultimap.create()

    private val GEAR_SET_SENDER = Identifier(GC.MOD_ID,"gear_set_sender")
    private val ACTIVE_SET_UPDATE = Identifier(GC.MOD_ID,"active_set_update")

    fun registerServer(){
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register{ server,_,_->
            getCachedSets()
            val maxIndex = setsToSend.size - 1
            for (player in server.playerManager.playerList){
                setsToSend.entries.forEachIndexed{ index, entry ->
                    val buf = PacketByteBufs.create()
                    buf.writeIdentifier(entry.key)
                    buf.writeString(entry.value)
                    buf.writeBoolean(index == maxIndex)
                    if (index == maxIndex){
                        buf.writeInt(cachedSetsToSend.asMap().size)
                        for (cachedSet in cachedSetsToSend.asMap().entries){
                            buf.writeIdentifier(cachedSet.key)
                            buf.writeInt(cachedSet.value.size)
                            for (set in cachedSet.value){
                                buf.writeIdentifier(set)
                            }
                        }
                    }
                    ServerPlayNetworking.send(player, GEAR_SET_SENDER,buf)
                }
            }
        }
        ServerPlayConnectionEvents.JOIN.register{handler,_,_->
            getCachedSets()
            val maxIndex = setsToSend.size - 1
            setsToSend.entries.forEachIndexed{ index, entry ->
                val buf = PacketByteBufs.create()
                buf.writeIdentifier(entry.key)
                buf.writeString(entry.value)
                buf.writeBoolean(index == maxIndex)
                if (index == maxIndex){
                    buf.writeInt(cachedSetsToSend.asMap().size)
                    for (cachedSet in cachedSetsToSend.asMap().entries){
                        buf.writeIdentifier(cachedSet.key)
                        buf.writeInt(cachedSet.value.size)
                        for (set in cachedSet.value){
                            buf.writeIdentifier(set)
                        }
                    }
                }
                ServerPlayNetworking.send(handler.player, GEAR_SET_SENDER, buf)
            }
        }
    }

    fun registerClient(){
        ClientPlayNetworking.registerGlobalReceiver(GEAR_SET_SENDER) { client, _, buf, _ ->
            val id = buf.readIdentifier()
            val jsonString = buf.readString()
            val bl = buf.readBoolean()
            val syncedCachedSets = if (bl){
                val hashMap: HashMultimap<Identifier,Identifier> = HashMultimap.create()
                val size1 = buf.readInt()
                for (i in 1..size1){
                    val itemId = buf.readIdentifier()
                    val size2 = buf.readInt()
                    for (j in 1..size2){
                        val setId = buf.readIdentifier()
                        hashMap.put(itemId,setId)
                    }
                }
                hashMap
            } else {
                HashMultimap.create()
            }
            client.execute {
                try {
                    println("Parsing Client set $id")
                    val json = JsonParser.parseString(jsonString).asJsonObject
                    gearSets[id] = (GearSet.fromJson(id, json))
                    if (bl) {
                        cachedSets.clear()
                        for (entry in syncedCachedSets.entries()){
                            val set = gearSets[entry.value] ?: continue
                            val item = Registries.ITEM.get(entry.key).takeIf { it != Items.AIR } ?: continue
                            cachedSets.put(item,set)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(ACTIVE_SET_UPDATE) { client, _, _, _ ->
            client.execute {
                val player = client.player ?: return@execute
                updateActiveSets(player)
            }
        }
        ItemTooltipCallback.EVENT.register{ stack, context, tooltip ->
            val player = MinecraftClient.getInstance().player ?: return@register
            val displaySets = cachedSets[stack.item]
            if (displaySets.isEmpty()) return@register
            val activeSets = (player as ActiveGearSetsTracking).gear_core_getActiveSets()
            for (displaySet in displaySets){
                val lvl = activeSets[displaySet] ?: 0
                displaySet.appendTooltip(lvl,stack,context,tooltip)
            }
        }
    }
    
    override fun reload(manager: ResourceManager) {
        val files = manager.findResources("gear_core_sets") { path -> path.path.endsWith(".json") }
        val gson = Gson()
        for (mutableEntry in files.entries) {
            try {
                val reader = mutableEntry.value.reader
                val json = JsonParser.parseReader(reader).asJsonObject
                println("reading set: ${mutableEntry.key}")
                val gearSet = GearSet.fromJson(mutableEntry.key, json)
                gearSets[mutableEntry.key] = gearSet
                if (FabricLoader.getInstance().environmentType == EnvType.SERVER){
                    setsToSend[mutableEntry.key] = gson.toJson(json)
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
        cachedSets.clear()
        cachedSetsToSend.clear()
    }

    private fun getCachedSets(): HashMultimap<Item,GearSet>{
        if (cachedSets.isEmpty)
            cacheSets()
        return cachedSets
    }

    private fun cacheSets(){
        cachedSets.clear()
        cachedSetsToSend.clear()
        println("Caching sets")
        for (item in Registries.ITEM){
            for (set in gearSets.values){
                if (set.test(item)){
                    cachedSets.put(item,set)
                    if (FabricLoader.getInstance().environmentType == EnvType.SERVER){
                        cachedSetsToSend.put(Registries.ITEM.getId(item),set.id)
                    }
                }
            }
        }
    }

    fun getGearSets(): Map<Identifier,GearSet>{
        return gearSets
    }

    fun updateActiveSets(entity: LivingEntity){
        val oldActiveMap = (entity as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (oldSet in oldActiveMap.keys){
            oldSet.removeAttributesFromEntity(entity)
        }
        val newActiveMap: HashMap<GearSet,Int> = hashMapOf()
        for (slot in EquipmentSlot.values()){
            val stack = entity.getEquippedStack(slot)
            if (!stack.isEmpty){
                val gearSets = getCachedSets().get(stack.item)
                for (gearSet in gearSets){
                    val num = newActiveMap[gearSet] ?: 0
                    newActiveMap[gearSet] = num + 1
                }
            }
        }
        if (TrinketChecker.trinketsLoaded) {
            val stacks = TrinketUtil.getTrinketStacks(entity)
            for (stack in stacks) {
                val gearSets = getCachedSets().get(stack.item)
                for (gearSet in gearSets){
                    val num = newActiveMap[gearSet] ?: 0
                    newActiveMap[gearSet] = num + 1
                }
            }
        }
        for (entry in newActiveMap.entries){
            entry.key.addAttributesToEntity(entity,entry.value)
        }
        (entity as ActiveGearSetsTracking).gear_core_setActiveSets(newActiveMap)
        //println(newActiveMap)
        if (entity is ServerPlayerEntity){
            ServerPlayNetworking.send(entity,ACTIVE_SET_UPDATE,PacketByteBufs.create())
        }
    }

    fun processPostHit(target: LivingEntity, attacker: PlayerEntity){
        val activeGearSets = (attacker as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (activeGearSet in activeGearSets){
            activeGearSet.key.processPostHit(activeGearSet.value,target, attacker)
        }
    }

    fun processPostMine(world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity){
        val activeGearSets = (miner as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (activeGearSet in activeGearSets){
            activeGearSet.key.processPostMine(activeGearSet.value,world, state, pos, miner)
        }
    }

    fun processOnUse(hand: Hand, user: PlayerEntity){
        val activeGearSets = (user as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (activeGearSet in activeGearSets){
            activeGearSet.key.processOnUse(activeGearSet.value, hand, user)
        }
    }

    fun processOnAttack(amount: Float, source: DamageSource, entity: LivingEntity, attacker: LivingEntity?): Float{
        val activeGearSets = (entity as ActiveGearSetsTracking).gear_core_getActiveSets()
        var newAmount = amount
        for (activeGearSet in activeGearSets){
            newAmount = activeGearSet.key.processOnAttack(activeGearSet.value,newAmount, source, entity, attacker)
        }
        return newAmount
    }

    fun processOnDamaged(amount: Float, source: DamageSource, entity: LivingEntity, attacker: LivingEntity?): Float{
        val activeGearSets = (entity as ActiveGearSetsTracking).gear_core_getActiveSets()
        var newAmount = amount
        for (activeGearSet in activeGearSets){
            newAmount = activeGearSet.key.processOnDamaged(activeGearSet.value,newAmount, source, entity, attacker)
        }
        return newAmount
    }

    fun processOnKilledOther(playerEntity: PlayerEntity, victim: LivingEntity?){
        val activeGearSets = (playerEntity as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (activeGearSet in activeGearSets){
            activeGearSet.key.processOnKilledOther(activeGearSet.value,playerEntity, victim)
        }
    }

    fun processTick(entity: LivingEntity){
        val activeGearSets = (entity as ActiveGearSetsTracking).gear_core_getActiveSets()
        for (activeGearSet in activeGearSets){
            activeGearSet.key.processTick(activeGearSet.value,entity)
        }
    }

    override fun getFabricId(): Identifier {
        return Identifier(GC.MOD_ID,"gear_sets_loader")
    }
}
