package me.fzzyhmstrs.amethyst_core.item_util

import com.google.common.collect.Multimap
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Flavorful
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import me.fzzyhmstrs.amethyst_core.trinket_util.AugmentTasks
import net.minecraft.client.item.TooltipContext
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

open class AbstractAugmentJewelryItem(settings: Settings): TrinketItem(settings), AugmentTasks, Flavorful<AbstractAugmentJewelryItem> {

    override var glint: Boolean = false
    override var flavor: String = ""
    override var flavorDesc: String = ""

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        addFlavorText(tooltip, context)
    }

    override fun getModifiers(
        stack: ItemStack,
        slot: SlotReference,
        entity: LivingEntity,
        uuid: UUID
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val modifiers = super.getModifiers(stack, slot, entity, uuid)
        modifierEnchantmentTasks(stack,entity.world,entity, modifiers)
        return modifiers
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onEquip(stack, slot, entity)
        if (entity.world.isClient()) return
        equipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onUnequip(stack, slot, entity)
        if(entity.world.isClient()) return
        unequipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        if(entity.world.isClient()) return
        if (EventRegistry.ticker_30.isReady()){
            intermittentTick(stack, entity)
        }
    }

    open fun intermittentTick(stack: ItemStack, entity: LivingEntity){
        passiveEnchantmentTasks(stack,entity.world,entity)
    }

    override fun getFlavorItem(): AbstractAugmentJewelryItem {
        return this
    }
}
