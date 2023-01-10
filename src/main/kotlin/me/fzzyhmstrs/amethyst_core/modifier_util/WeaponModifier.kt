package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*

class WeaponModifier(modifierId: Identifier = ModifierDefaults.BLANK_ID,val persistent: Boolean = false, val randomSelectable: Boolean = false): AbstractModifier<WeaponModifier>(modifierId) {

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
    private val postHitConsumers: MutableList<WeaponConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<WeaponConsumer> = mutableListOf()
    private val tickConsumers: MutableList<WeaponConsumer> = mutableListOf()

    override fun plus(other: WeaponModifier): WeaponModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): WeaponModifier{
        val modifier = EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun withOnHit(onHit: WeaponConsumer): WeaponModifier{
        postHitConsumers.add(onHit)
        return this
    }

    fun withOnUse(onUse: WeaponConsumer): WeaponModifier{
        onUseConsumers.add(onUse)
        return this
    }

    fun withTick(tick: WeaponConsumer): WeaponModifier{
        tickConsumers.add(tick)
        return this
    }
    
    fun withDescendant(modifier: WeaponModifier): WeaponModifier {
        addDescendant(modifier)
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(mutableListOf(), WeaponModifier())
    }

    override fun getTranslationKey(): String {
        return "weapon.modifier.${modifierId}"
    }

    @FunctionalInterface
    fun interface WeaponConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)
    }
}
