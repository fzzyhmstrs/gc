package me.fzzyhmstrs.viscerae.modifier

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlF
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlI
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierDefaults
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class WeaponModifier(modifierId: Identifier = ModifierDefaults.BLANK_ID,val persistent: Boolean = false, val randomSelectable: Boolean = false): AbstractModifier<WeaponModifier>(modifierId) {

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
    private val postHitConsumers: MutableList<WeaponConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<WeaponConsumer> = mutableListOf()
    private val tickConsumers: MutableList<WeaponConsumer> = mutableListOf()

    override fun plus(other: WeaponModifier): WeaponModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        tickConsumers.addAll(other.tickConsumers)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, modifier: EntityAttributeModifier): WeaponModifier{
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun withPostHit(onHit: WeaponConsumer): WeaponModifier{
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

    interface WeaponConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)
    }
}
