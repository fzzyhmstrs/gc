package me.fzzyhmstrs.amethyst_core.item_util

import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder
import net.minecraft.util.Identifier

/**
 * Used to build a loot pool for the [LootRegistry][me.fzzyhmstrs.amethyst_core.registry.LootRegistry]. See Amethyst Imbuement for examples.
 *
 * (targeted for further functionality buildout in the future)
 */
abstract class AbstractModLoot {

    abstract val targetNameSpace: String

    abstract fun lootBuilder(id: Identifier, table: FabricLootTableBuilder): Boolean

    internal fun buildLoot(id: Identifier, table: FabricLootTableBuilder): Boolean{
        if (id.namespace != targetNameSpace) return false
        return lootBuilder(id, table)
    }

}