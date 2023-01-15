package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld

interface KillTracking {
    fun onWearerKilledOther(stack: ItemStack, wearer: LivingEntity, victim: LivingEntity, world: ServerWorld)
}