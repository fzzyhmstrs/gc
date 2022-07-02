package me.fzzyhmstrs.amethyst_core.mana_util

import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import net.minecraft.item.ItemStack

/**
 * Helper object for mana items that self-heal. A simple initialization method that maps the provided stack to a [Ticker][me.fzzyhmstrs.amethyst_core.registry.EventRegistry.Ticker]
 */
object ManaHelper {

    private val scepterHealTickers: MutableMap<Long, EventRegistry.Ticker> = mutableMapOf()

    /**
     * call this to add the ManaItem stack into the healing queue. Will not automatically heal the stack with this call.
     */
    fun initializeManaItem(stack: ItemStack){
        val id = Nbt.makeItemStackId(stack)
        if (!scepterHealTickers.containsKey(id)){
            val item = stack.item
            if (item is ManaItem) {
                scepterHealTickers[id] = EventRegistry.Ticker(item.getRepairTime())
            }
        }
    }

    /**
     * add a call to this method in the items tick to know when to use [ManaItem.healDamage] as needed in your implementation.
     */
    fun tickHeal(stack: ItemStack): Boolean{
        val id = Nbt.makeItemStackId(stack)
        val ticker = scepterHealTickers[id]?:return false
        ticker.tickUp()
        return ticker.isReady()
    }

    fun needsInitialization(stack: ItemStack): Boolean{
        val id = Nbt.getItemStackId(stack)
        return !scepterHealTickers.containsKey(id)
    }

}