package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

interface HitTracking {
    fun postWearerHit(stack: ItemStack, wearer: LivingEntity, target: LivingEntity)
}