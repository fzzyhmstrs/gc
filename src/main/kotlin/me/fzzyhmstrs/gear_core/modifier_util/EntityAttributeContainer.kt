package me.fzzyhmstrs.gear_core.modifier_util

import me.fzzyhmstrs.fzzy_core.modifier_util.SlotId
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.attribute.EntityAttributeModifier
import java.util.*

class EntityAttributeContainer(private val name: String, private val amount: Double, private val operation: EntityAttributeModifier.Operation, private val tierToMultiplier: Expression = Expression.parse("t * a")){
    private val attributes: MutableMap<SlotId, MutableMap<Int, EntityAttributeModifier>> = mutableMapOf()
    private val evalMap: MutableMap<Char,Double> = mutableMapOf('t' to 1.0, 'a' to amount)

    fun provideEntityAttribute(slot: SlotId, offset: Int, tier: Int): EntityAttributeModifier {
        evalMap['t'] = tier.toDouble()
        val index = tier * 1000000 + offset
        return attributes.computeIfAbsent(slot) {mutableMapOf()}.computeIfAbsent(index) {EntityAttributeModifier(slot.getUUID("$name#$offset"),name,eval(),operation)}
    }

    private fun eval(): Double{
        return tierToMultiplier.eval(evalMap())
    }

    companion object {
        val CODEC 
    }
}
