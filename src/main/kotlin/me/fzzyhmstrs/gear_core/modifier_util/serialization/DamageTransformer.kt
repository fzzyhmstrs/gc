package me.fzzyhmstrs.gear_core.modifier_util.serialization

import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier

interface DamageTransformer: EquipmentModifier.DamageFunction {
    fun getType(): DamageTransformerType<*>
}