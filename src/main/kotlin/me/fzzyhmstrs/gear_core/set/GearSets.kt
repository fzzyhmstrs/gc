package me.fzzyhmstrs.gear_core.set

import com.google.common.collect.HashMultimap
import com.google.gson.JsonParser
import me.fzzyhmstrs.gear_core.GC
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.item.Item
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object GearSets: SimpleSynchronousResourceReloadListener {

    private var gearSets: Set<GearSet> = setOf()
    private var cachedSets: HashMultimap<Item,GearSet> = HashMultimap.create()

    override fun reload(manager: ResourceManager) {
        val gearSets: MutableSet<GearSet> = mutableSetOf()
        manager.findResources("gear_core/sets") { path -> path.path.endsWith(".json") }
        .forEach { (id,resource) ->
            try {
                val reader = resource.reader
                val json = JsonParser.parseReader(reader).asJsonObject
                gearSets.add(GearSet.fromJson(id, json))
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
