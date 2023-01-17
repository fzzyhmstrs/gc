package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*

class ToolModifier(modifierId: Identifier = ModifierDefaults.BLANK_ID, val persistent: Boolean = false, val randomSelectable: Boolean = false): AbstractModifier<ToolModifier>(modifierId) {

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
    private val postHitConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val postMineConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val tickConsumers: MutableList<ToolConsumer> = mutableListOf()

    override fun plus(other: ToolModifier): ToolModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        postMineConsumers.addAll(other.postMineConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        tickConsumers.addAll(other.tickConsumers)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): ToolModifier {
        val modifier = EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun withPostHit(onHit: ToolConsumer): ToolModifier {
        postHitConsumers.add(onHit)
        return this
    }

    fun withPostMine(onMine: ToolConsumer): ToolModifier {
        postMineConsumers.add(onMine)
        return this
    }

    fun withOnUse(onUse: ToolConsumer): ToolModifier {
        onUseConsumers.add(onUse)
        return this
    }

    fun withTick(tick: ToolConsumer): ToolModifier {
        tickConsumers.add(tick)
        return this
    }
    
    fun withDescendant(modifier: ToolModifier): ToolModifier {
        addDescendant(modifier)
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(mutableListOf(), ToolModifier())
    }

    override fun getTranslationKey(): String {
        return "tool.modifier.${modifierId}"
    }

    @FunctionalInterface
    fun interface ToolConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)
    }
}
