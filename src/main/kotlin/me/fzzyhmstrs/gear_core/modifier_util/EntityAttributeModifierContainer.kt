package me.fzzyhmstrs.gear_core.modifier_util

import me.fzzyhmstrs.fzzy_core.modifier_util.SlotId
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributeModifier
import java.util.*

class EntityAttributeModifierContainer(private val name: String, private val amount: Double, private val operation: EntityAttributeModifier.Operation, private val uuid: UUID? = null){
    private val attributes: MutableMap<SlotId, EntityAttributeModifier> = mutableMapOf()

    fun provideEntityAttribute(slot: SlotId, offset: Int): EntityAttributeModifier {
        return attributes.computeIfAbsent(slot) {EntityAttributeModifier(slot.getUUID("$name#$offset"),name,amount,operation)}
    }

    fun provideNonSlotEntityAttribute(): EntityAttributeModifier {
        return EntityAttributeModifier(uuid?:UUID.randomUUID(),name,amount,operation)
    }
}
