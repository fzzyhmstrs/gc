package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.item_util.AbstractModLoot
import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.loot.v2.LootTableSource
import net.minecraft.loot.LootManager
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

/**
 * simple registry for adding a custom [AbstractModLoot] loot pool to a pre-existing loot table.
 */
object LootRegistry {

    private val modLoots: MutableList<AbstractModLoot> = mutableListOf()

    internal fun registerAll(){

        LootTableEvents.MODIFY.register { _: ResourceManager, _: LootManager, id: Identifier, table: FabricLootTableBuilder, _: LootTableSource ->
            if (modLoots.isEmpty()) return@register
            for (modLoot in modLoots) {
                if (modLoot.buildLoot(id, table)) return@register
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