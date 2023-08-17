package me.fzzyhmstrs.gear_core.modifier_util

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributeModifier
import java.util.*

class EntityAttributeModifierContainer(private val name: String, private val amount: Double, private val operation: EntityAttributeModifier.Operation, private val uuid: UUID? = null){
    private val attributes: EnumMap<EquipmentSlot, EntityAttributeModifier> = EnumMap(mapOf(
        EquipmentSlot.MAINHAND to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation),
        EquipmentSlot.OFFHAND to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation),
        EquipmentSlot.HEAD to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation),
        EquipmentSlot.CHEST to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation),
        EquipmentSlot.LEGS to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation),
        EquipmentSlot.FEET to EntityAttributeModifier(UUID.randomUUID(),name,amount,operation)
    ))

    fun provideEntityAttribute(slot: EquipmentSlot): EntityAttributeModifier {
        return attributes[slot]?:throw IllegalStateException("slot $slot not found in enum-map for some reason!")
    }

    fun provideNonSlotEntityAttribute(): EntityAttributeModifier {
        return EntityAttributeModifier(uuid?:UUID.randomUUID(),name,amount,operation)
    }
}