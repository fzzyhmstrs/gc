package me.fzzyhmstrs.amethyst_core.item_util

import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.minecraft.util.Identifier

abstract class AbstractModLoot {

    abstract val targetNameSpace: String

    abstract fun lootBuilder(id: Identifier, table: FabricLootSupplierBuilder): Boolean

    internal fun buildLoot(id: Identifier, table: FabricLootSupplierBuilder): Boolean{
        if (id.namespace != targetNameSpace) return false
        return lootBuilder(id, table)
    }

}