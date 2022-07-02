package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.item_util.AbstractModLoot
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.loot.LootManager
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/**
 * simple registry for adding a custom [AbstractModLoot] loot pool to a pre-existing loot table.
 */
object LootRegistry {

    private val modLoots: MutableList<AbstractModLoot> = mutableListOf()

    internal fun registerAll(){

        LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _: ResourceManager, _: LootManager, id: Identifier, table: FabricLootSupplierBuilder, _: LootTableLoadingCallback.LootTableSetter ->
            if (modLoots.isEmpty()) return@LootTableLoadingCallback
            for (modLoot in modLoots) {
                if (modLoot.buildLoot(id, table)) return@LootTableLoadingCallback
            }
        })
    }

    /**
     * register your custom loot pool with this method.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun registerModLoot(modLoot: AbstractModLoot){
        modLoots.add(modLoot)
    }
}