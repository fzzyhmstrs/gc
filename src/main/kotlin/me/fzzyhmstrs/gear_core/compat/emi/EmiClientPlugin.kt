package me.fzzyhmstrs.gear_core.compat.emi

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiStack
import me.fzzyhmstrs.gear_core.GC
import me.fzzyhmstrs.gear_core.set.GearSets
import net.minecraft.item.Items
import net.minecraft.util.Identifier

object EmiClientPlugin: EmiPlugin {

    private val GEAR_SET_WORKSTATION: EmiStack = EmiStack.of(Items.NETHERITE_CHESTPLATE)
    val GEAR_SET_CATEGORY = EmiRecipeCategory(Identifier(GC.MOD_ID,"gear_sets"), GEAR_SET_WORKSTATION)
    override fun register(registry: EmiRegistry) {

        registry.addCategory(GEAR_SET_CATEGORY)

        println(GearSets.getGearSets())

        for (entry in GearSets.getGearSets()){
            registry.addRecipe(GearSetEmiRecipe(entry.key,entry.value))
        }

    }
}