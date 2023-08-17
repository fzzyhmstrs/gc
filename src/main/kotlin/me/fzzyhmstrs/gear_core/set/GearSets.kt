package me.fzzyhmstrs.gear_core.set

import com.google.common.collect.HashMultimap
import com.google.gson.JsonParser
import me.fzzyhmstrs.gear_core.GC
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.item.Item
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object GearSets: SimpleSynchronousResourceReloadListener {

    private val gearSets: MutableSet<GearSet> = mutableSetOf()
    private val cachedSets: HashMultimap<Item,GearSet> = HashMultimap.create()
    private val 

    private val GEAR_SET_SENDER = Identifier(GC.MOD_ID,"gear_set_sender")
    
    fun registerServer(){
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
    }

    fun registerClient{
        ClientPlayNetworking.registerGlobalReceiver(GEAR_SET_SENDER) {client,_,buf,_ ->
            val jsonString = buf.readString()
            val json = JsonParser().parse(jsonString).asJsonObject
            if (buf.readBoolean()){
                cachedSets.clear()
                for (item in Registries.ITEM){
                    for (set in gearSets){
                        if (set.test(item)){
                            cachedSets.put(item,set)
                        }
                    }
                }
            }
        }
    }
    
    override fun reload(manager: ResourceManager) {
        val gearSets: MutableSet<GearSet> = mutableSetOf()
        manager.findResources("gear_core/sets") { path -> path.path.endsWith(".json") }
        .forEach { (id,resource) ->
            try {
                val reader = resource.reader
                val json = JsonParser.parseReader(reader).asJsonObject
                gearSets.add(GearSet.fromJson(id, json))
                if (FabricLoader.getInstance().environmentType == EnvType.SERVER){
                    val buf = PacketByteBufs.create()
                    buf.writeString(TODO())
                    
                    ServerPlayNetworking.send
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
        cachedSets.clear()
        for (item in Registries.ITEM){
            for (set in gearSets){
                if (set.test(item)){
                    cachedSets.put(item,set)
                }
            }
        }
    }

    fun updateActiveSets(entity: LivingEntity){
        val oldActiveMap = (entity as ActiveGearSetTracking).getActiveSets()
        for (oldSet in oldActiveMap.keySet()){
            oldSet.removeAttributesFromEntity(entity)
        }
        val newActiveMap: HashMap<GearSet,Int> = hashMapOf()
        for (slot in EquipmentSlot.entries){
            val stack = entity.getEquippedStack(slot)
            if (!stack.isEmpty()){
                val gearSets = cachedSets.get(stack.item)
                for (gearSet in gearSets){
                    val num = newActiveMap[gearSet] ?: 0
                    newActiveMap[gearSet] = num + 1
                }
            }
        }
        if (TrinketChecker.getTrinketsLoaded()) {
            val stacks = TrinketUtil.getTrinketStacks(entity)
            for (stack in stacks) {
                val gearSets = cachedSets.get(stack.item)
                for (gearSet in gearSets){
                    val num = newActiveMap[gearSet] ?: 0
                    newActiveMap[gearSet] = num + 1
                }
            }
        }
        for (entry in newActiveMap.entrySet()){
            entry.key.addAttributesToEntity(entity,entry.value)
        }
        (entity as ActiveGearSetTracking).setActiveSets(newActiveMap)
    }

    override fun getFabricId(): Identifier {
        return Identifier(GC.MOD_ID,"gear_sets_loader")
    }
}
