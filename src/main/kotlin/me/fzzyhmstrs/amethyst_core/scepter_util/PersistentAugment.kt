package me.fzzyhmstrs.amethyst_imbuement.scepter.base_augments

import me.fzzyhmstrs.amethyst_core.scepter_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.PerLvlI
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface PersistentAugment {

    val delay: PerLvlI

    fun persistentEffect(world: World, user: LivingEntity, blockPos: BlockPos, entityList: MutableList<Entity>, level: Int = 1, effect: AugmentEffect)
}