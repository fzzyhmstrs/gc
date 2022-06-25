package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.misc_util.AbstractModLoot
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.loot.LootManager
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object LootRegistry {

    private val modLoots: MutableList<AbstractModLoot> = mutableListOf()

    fun registerAll(){

        LootTableLoadingCallback.EVENT.register(LootTableLoadingCallback { _: ResourceManager, _: LootManager, id: Identifier, table: FabricLootSupplierBuilder, _: LootTableLoadingCallback.LootTableSetter ->
            if (modLoots.isEmpty()) return@LootTableLoadingCallback
            for (modLoot in modLoots) {
                if (modLoot.lootBuilder(id, table)) return@LootTableLoadingCallback
            }
        })
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun registerModLoot(modLoot: AbstractModLoot){
        modLoots.add(modLoot)
    }
}