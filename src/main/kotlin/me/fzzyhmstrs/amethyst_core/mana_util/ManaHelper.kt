package me.fzzyhmstrs.amethyst_core.mana_util

import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import net.minecraft.item.ItemStack

object ManaHelper {

    private val scepterHealTickers: MutableMap<ItemStack, EventRegistry.Ticker> = mutableMapOf()

    fun initializeManaItem(stack: ItemStack){
        if (!scepterHealTickers.containsKey(stack)){
            val item = stack.item
            if (item is ManaItem) {
                scepterHealTickers[stack] = EventRegistry.Ticker(item.getRepairTime())
            }
        }
    }

    fun tickHeal(id: ItemStack): Boolean{
        val ticker = scepterHealTickers[id]?:return false
        ticker.tickUp()
        return ticker.isReady()
    }

    fun needsInitialization(stack: ItemStack): Boolean{
        return !scepterHealTickers.containsKey(stack)
    }

}