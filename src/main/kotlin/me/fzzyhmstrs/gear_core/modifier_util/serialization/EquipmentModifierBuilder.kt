package me.fzzyhmstrs.gear_core.modifier_util.serialization

import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierConsumer
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class EquipmentModifierBuilder(
    val target: EquipmentModifier.EquipmentModifierTarget,
    val weight: Int,
    val rarity: EquipmentModifier.Rarity,
    val persistent: Boolean,
    val randomSelectable: Boolean,
    val customFormatting: List<Formatting>,
    val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier>,
    val modifierModifiers: List<Identifier>,
    val postHitConsumers: List<ModifierConsumer>
) {
}