package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

/**
 * Extends the [AbstractActiveAugment] with a useEffect. Might be called every time [use][net.minecraft.item.Item.use] is, rather than switching between acitvate and deactivate as with the ActiveAugment.
 */
abstract class AbstractUsedActiveAugment(weight: Rarity, mxLvl: Int = 1, vararg slot: EquipmentSlot): AbstractActiveAugment(weight,mxLvl,*slot) {

    internal fun baseUseEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        if (!enabled) return
        useEffect(user, level, stack)
    }

    open fun useEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

}