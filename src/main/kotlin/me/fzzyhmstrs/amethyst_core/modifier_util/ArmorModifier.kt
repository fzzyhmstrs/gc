package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*

class ArmorModifier(modifierId: Identifier = ModifierDefaults.BLANK_ID, val persistent: Boolean = false, val randomSelectable: Boolean = false): AbstractModifier<ArmorModifier>(modifierId) {

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
    private val postHitConsumers: MutableList<ArmorConsumer> = mutableListOf()
    private val onDamagedConsumers: MutableList<ArmorConsumer> = mutableListOf()
    private val tickConsumers: MutableList<ArmorConsumer> = mutableListOf()

    override fun plus(other: ArmorModifier): ArmorModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        onDamagedConsumers.addAll(other.postHitConsumers)
        tickConsumers.addAll(other.tickConsumers)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): ArmorModifier {
        val modifier = EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun withPostHit(onHit: ArmorConsumer): ArmorModifier {
        postHitConsumers.add(onHit)
        return this
    }

    fun withOnDamaged(onDamaged: ArmorConsumer): ArmorModifier {
        onDamagedConsumers.add(onDamaged)
        return this
    }

    fun withTick(tick: ArmorConsumer): ArmorModifier {
        tickConsumers.add(tick)
        return this
    }
    
    fun withDescendant(modifier: ArmorModifier): ArmorModifier {
        addDescendant(modifier)
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(mutableListOf(), ArmorModifier())
    }

    override fun getTranslationKey(): String {
        return "armor.modifier.${modifierId}"
    }

    @FunctionalInterface
    fun interface ArmorConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?)
    }
}
