package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

/**
 * Extension of the [BaseAugment] that has methods to activate and deactivate an effect. This is typically done with the Item [use][net.minecraft.item.Item.use] method, but isn't necessarily implemented that way.
 */
abstract class AbstractActiveAugment(weight: Rarity, mxLvl: Int = 1, vararg slot: EquipmentSlot): BaseAugment(weight,mxLvl,EnchantmentTarget.ARMOR, *slot) {

    internal fun baseActivateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        if (!enabled)return
        activateEffect(user, level, stack)
    }

    open fun activateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

    internal fun baseDeactivateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        if (!enabled)return
        deactivateEffect(user, level, stack)
    }

    open fun deactivateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }
}