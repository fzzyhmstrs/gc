package me.fzzyhmstrs.amethyst_core.trinket_util

import dev.emi.trinkets.api.TrinketsApi
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

/**
 * simple util to check for the Trinket items equipped to a LivingEntity.
 */
object TrinketUtil {

    fun getTrinketStacks(user: LivingEntity): List<ItemStack>{
        val list: MutableList<ItemStack> = mutableListOf()
        val comp = TrinketsApi.getTrinketComponent(user)
        if (comp.isPresent) {
            val items = comp.get().allEquipped
            for (slot in items) {
                list.add(slot.right)
            }
        }
        return list
    }
}