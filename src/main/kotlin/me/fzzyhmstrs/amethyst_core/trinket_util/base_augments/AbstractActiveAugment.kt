package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

/**
 * Extension of the [BaseAugment] that has methods to activate and deactivate an effect. This is typically done with the Item [use][net.minecraft.item.Item.use] method, but isn't necessarily implemented that way.
 */
open class AbstractActiveAugment(weight: Rarity, mxLvl: Int = 1, vararg slot: EquipmentSlot): BaseAugment(weight,mxLvl,EnchantmentTarget.ARMOR, *slot) {

    open fun activateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

    open fun deactivateEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }
}