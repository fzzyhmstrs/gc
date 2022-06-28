package me.fzzyhmstrs.amethyst_core.modifier_util

import net.minecraft.util.Identifier

class CompiledAugmentModifier(
    modifierId: Identifier = ModifierDefaults.BLANK_ID,
    override var levelModifier: Int = 0,
    override var cooldownModifier: Double = 0.0,
    override var manaCostModifier: Double = 0.0)
    : AugmentModifier(modifierId, levelModifier, cooldownModifier, manaCostModifier){
}