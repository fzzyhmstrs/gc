package me.fzzyhmstrs.amethyst_core.item_util

import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import me.fzzyhmstrs.amethyst_core.trinket_util.AugmentTasks
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*

open class AbstractAugmentJewelryItem(settings: Settings, private val id: Identifier):TrinketItem(settings), AugmentTasks {


    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip?.add(TranslatableText("item.${id.namespace}.${id.path}.tooltip1").formatted(Formatting.WHITE, Formatting.ITALIC))
    }

    override fun getModifiers(
        stack: ItemStack,
        slot: SlotReference,
        entity: LivingEntity,
        uuid: UUID
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val modifiers = super.getModifiers(stack, slot, entity, uuid)
        modifiers.putAll(getAugmentModifiers(stack, entity, uuid))
        return modifiers
    }

    open fun getAugmentModifiers(
        stack: ItemStack,
        entity: LivingEntity,
        uuid: UUID
    ): Multimap<EntityAttribute, EntityAttributeModifier>{
        val map = Multimaps.newMultimap(
            Maps.newLinkedHashMap<EntityAttribute, Collection<EntityAttributeModifier>>()
        ) { ArrayList() }
        modifierEnchantmentTasks(stack,entity.world,entity, map)
        return map
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onEquip(stack, slot, entity)
        if (entity.world.isClient()) return
        jewelryEquip(stack, entity)
    }

    open fun jewelryEquip(stack: ItemStack, entity: LivingEntity){
        equipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onUnequip(stack, slot, entity)
        if(entity.world.isClient()) return
        jewelryUnEquip(stack, entity)
    }

    open fun jewelryUnEquip(stack: ItemStack, entity: LivingEntity){
        unequipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        if(entity.world.isClient()) return
        if (EventRegistry.ticker_30.isReady()){
            jewelryIntermittentTick(stack, entity)
        }
    }

    open fun jewelryIntermittentTick(stack: ItemStack, entity: LivingEntity){
        passiveEnchantmentTasks(stack,entity.world,entity)
    }

    override fun passiveEnchantmentTasks(stack: ItemStack,world: World,entity: Entity){
        if (!entity.isPlayer) return
        super.passiveEnchantmentTasks(stack, world, entity)
    }
}