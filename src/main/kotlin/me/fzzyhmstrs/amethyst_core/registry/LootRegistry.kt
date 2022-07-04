package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.item_util.AbstractModLoot
import net.fabricmc.fabric.api.loot.v2.LootTableEvents

/**
 * simple registry for adding a custom [AbstractModLoot] loot pool to a pre-existing loot table.
 */
object LootRegistry {

    private val modLoots: MutableList<AbstractModLoot> = mutableListOf()

    internal fun registerAll(){

        LootTableEvents.MODIFY.register { _, _, id, tableBuilder, _ ->
            if (modLoots.isEmpty()) return@register
            for (modLoot in modLoots) {
                if (modLoot.lootBuilder(id, tableBuilder)) break
            }
        }
    }

    /**
     * register your custom loot pool with this method.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun registerModLoot(modLoot: AbstractModLoot){
        modLoots.add(modLoot)
    }
}