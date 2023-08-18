package me.fzzyhmstrs.gear_core

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer
import me.fzzyhmstrs.gear_core.interfaces.ActiveGearSetsTracking
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import me.fzzyhmstrs.gear_core.set.GearSets
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


object GC: ModInitializer {
    const val MOD_ID = "gear_core"
    val EQUIPMENT_MODIFIER_TYPE = Registry.register(ModifierHelperType.REGISTRY, EquipmentModifierType.id,EquipmentModifierType)

    object EquipmentModifierType: ModifierHelperType(Identifier(MOD_ID,"gear_modifier_helper")){
        override fun getModifierIdKey(): String {
            return "gear_modifier_id"
        }
        override fun getModifiersKey(): String {
            return "gear_modifiers"
        }
        override fun getModifierInitializer(): ModifierInitializer {
            return EquipmentModifierHelper
        }
    }

    override fun onInitialize() {
        GearSets.registerServer()
    }
}

object GCClient: ClientModInitializer{
    override fun onInitializeClient() {
        GearSets.registerClient()
        ItemTooltipCallback.EVENT.register{stack,context,tooltip ->
            val player = MinecraftClient.getInstance().player ?: return@register
            val sets = (player as ActiveGearSetsTracking).gear_core_getActiveSets()
        }
    }

}

object GCPreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {
        MixinExtrasBootstrap.init()
    }


}