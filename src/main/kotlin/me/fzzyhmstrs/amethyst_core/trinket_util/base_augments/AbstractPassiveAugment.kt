package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack


open class AbstractPassiveAugment(weight: Rarity, mxLvl: Int = 1, vararg slot: EquipmentSlot): BaseAugment(weight,mxLvl,EnchantmentTarget.ARMOR, *slot) {

    open fun tickEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

}