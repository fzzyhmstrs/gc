package me.fzzyhmstrs.amethyst_core.item_util

import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.minecraft.util.Identifier

/**
 * Used to build a loot pool for the [LootRegistry][me.fzzyhmstrs.amethyst_core.registry.LootRegistry]. See Amethyst Imbuement for examples.
 *
 * (targeted for further functionality buildout in the future)
 */
abstract class AbstractModLoot {

    abstract val targetNameSpace: String

    abstract fun lootBuilder(id: Identifier, table: FabricLootSupplierBuilder): Boolean

    internal fun buildLoot(id: Identifier, table: FabricLootSupplierBuilder): Boolean{
        if (id.namespace != targetNameSpace) return false
        return lootBuilder(id, table)
    }

}