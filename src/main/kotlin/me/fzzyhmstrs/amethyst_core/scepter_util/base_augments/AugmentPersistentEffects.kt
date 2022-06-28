package me.fzzyhmstrs.amethyst_core.scepter_util.base_augments

import me.fzzyhmstrs.amethyst_core.coding_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class AugmentPersistentEffectData(val world: World, val user: LivingEntity,
                                  val blockPos: BlockPos, val entityList: MutableList<Entity>,
                                  val level: Int = 1, val effect: AugmentEffect
): PersistentEffectHelper.PersistentEffectData


interface AugmentPersistentEffect: PersistentEffectHelper.PersistentEffect {
    override fun persistentEffect(data: PersistentEffectHelper.PersistentEffectData){
        if (data !is AugmentPersistentEffectData) return
        augmentPersistentEffect(
            data.world,
            data.user,
            data.blockPos,
            data.entityList,
            data.level,
            data.effect
        )
    }

    fun augmentPersistentEffect(world: World, user: LivingEntity, blockPos: BlockPos, entityList: MutableList<Entity>, level: Int = 1, effect: AugmentEffect)
}