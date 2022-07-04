package me.fzzyhmstrs.amethyst_core.item_util

import net.minecraft.loot.LootTable
import net.minecraft.util.Identifier

/**
 * Used to build a loot pool for the [LootRegistry][me.fzzyhmstrs.amethyst_core.registry.LootRegistry]. See Amethyst Imbuement for examples.
 *
 * (targeted for further functionality buildout in the future)
 */
abstract class AbstractModLoot {

    abstract val targetNameSpace: String

    abstract fun lootBuilder(id: Identifier, table: LootTable.Builder): Boolean

    internal fun buildLoot(id: Identifier, table: LootTable.Builder): Boolean{
        if (id.namespace != targetNameSpace) return false
        return lootBuilder(id, table)
    }

}