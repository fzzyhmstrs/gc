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

    }



    override fun getFabricId(): Identifier {
        return Identifier(GC.MOD_ID,"gear_sets_loader")
    }
}