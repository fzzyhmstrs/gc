package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack


open class AbstractEquipmentAugment(weight: Rarity, mxLvl: Int = 1, target: EnchantmentTarget = EnchantmentTarget.ARMOR, vararg slot: EquipmentSlot): BaseAugment(weight,mxLvl,target, *slot) {

    open fun equipmentEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }
}