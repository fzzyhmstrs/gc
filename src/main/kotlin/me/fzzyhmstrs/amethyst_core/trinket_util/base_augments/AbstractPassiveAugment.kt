package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

/**
 * extension of [BaseAugment] intended for use with a tick. Common usage includes repeated application of a status effect.
 */
abstract class AbstractPassiveAugment(weight: Rarity, mxLvl: Int = 1, vararg slot: EquipmentSlot): BaseAugment(weight,mxLvl,EnchantmentTarget.ARMOR, *slot) {

    override fun equipEffect(user: LivingEntity, level: Int, stack: ItemStack) {
        baseTickEffect(user, level, stack)
    }

    internal fun baseTickEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        if (!enabled) return
        tickEffect(user, level, stack)
    }

    open fun tickEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }
}