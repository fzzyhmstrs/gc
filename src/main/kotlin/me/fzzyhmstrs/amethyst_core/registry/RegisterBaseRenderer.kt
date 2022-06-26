package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.entity_util.MissileEntityRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRendererFactory

@Environment(value = EnvType.CLIENT)
object RegisterBaseRenderer {

    fun registerAll(){
        EntityRendererRegistry.register(
            RegisterBaseEntity.MISSILE_ENTITY
        ){context: EntityRendererFactory.Context ->
            MissileEntityRenderer(
                context
            )
        }
    }

}