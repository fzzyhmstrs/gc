package me.fzzyhmstrs.amethyst_core.trinket_util

import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketsApi
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import java.util.function.Predicate

/**
 * simple util to check for the Trinket items equipped to a LivingEntity.
 */
object TrinketUtil {

    val trinketCheck: Predicate<ItemStack> = Predicate { stack: ItemStack -> stack.item is Trinket}

    fun getTrinketStacks(user: LivingEntity): List<ItemStack>{
        val comp = TrinketsApi.getTrinketComponent(user)
        if (comp.isPresent) {
            val list: MutableList<ItemStack> = mutableListOf()
            val items = comp.get().allEquipped
            for (slot in items) {
                list.add(slot.right)
            }
            return list
        }
        return listOf()
    }
}