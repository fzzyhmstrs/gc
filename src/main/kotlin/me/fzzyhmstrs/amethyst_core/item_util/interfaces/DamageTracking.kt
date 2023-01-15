package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack

interface DamageTracking {
    fun onWearerDamaged(stack: ItemStack, wearer: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float)
}