package me.fzzyhmstrs.gear_core.set

import me.fzzyhmstrs.gear_core.GC
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object GearSets: SimpleSynchronousResourceReloadListener {
    override fun reload(manager: ResourceManager?) {
        TODO("Not yet implemented")
    }



    override fun getFabricId(): Identifier {
        return Identifier(GC.MOD_ID,"gear_sets_loader")
    }
}