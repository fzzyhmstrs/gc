package me.fzzyhmstrs.gear_core

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierContainer
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer
import me.fzzyhmstrs.fzzy_core.modifier_util.SlotId
import me.fzzyhmstrs.gear_core.interfaces.ActiveGearSetsTracking
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import me.fzzyhmstrs.gear_core.modifier_util.serialization.DamageTransformerType
import me.fzzyhmstrs.gear_core.modifier_util.serialization.MiningModifierConsumer
import me.fzzyhmstrs.gear_core.modifier_util.serialization.MiningModifierConsumerType
import me.fzzyhmstrs.gear_core.set.GearSets
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


object GC: ModInitializer {
    const val MOD_ID = "gear_core"
    val EQUIPMENT_MODIFIER_TYPE =  ModifierHelperType.register(EquipmentModifierType)

    object EquipmentModifierType: ModifierHelperType<EquipmentModifier>(Identifier(MOD_ID,"gear_modifier_helper"), EquipmentModifierHelper){
        override fun getModifierIdKey(): String {
            return "gear_modifier_id"
        }
        override fun getModifiersKey(): String {
            return "gear_modifiers"
        }
        override fun getModifierInitKey(): String {
            return "gc_"
        }
        override fun getModifierInitializer(): ModifierInitializer {
            return EquipmentModifierHelper
        }
        override fun add(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
            val mods = helper().getModifiersFromNbt(stack)
            modifierContainer.livingEntity.attributes.addTemporaryModifiers(EquipmentModifierHelper.prepareContainerMap(id,mods))
            //println("Adding modifiers to ${modifierContainer.livingEntity} from stack $stack")
            for (mod in mods.mapNotNull { helper().getModifierByType(it) }) {
                modifierContainer.addModifier(mod, this)
            }
        }

        override fun remove(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
            val mods = helper().getModifiersFromNbt(stack)
            modifierContainer.livingEntity.attributes.removeModifiers(EquipmentModifierHelper.prepareContainerMap(id,mods))
            for (mod in mods.mapNotNull { helper().getModifierByType(it) }) {
                modifierContainer.removeModifier(mod, this)
            }
        }
    }

    override fun onInitialize() {
        GearSets.registerServer()
        DamageTransformerType.Types.init()
        MiningModifierConsumerType.Types.init()
    }
}

object GCClient: ClientModInitializer{
    override fun onInitializeClient() {
        GearSets.registerClient()
    }

}

object GCPreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {
        MixinExtrasBootstrap.init()
    }


}