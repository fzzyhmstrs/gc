package me.fzzyhmstrs.amethyst_core.interfaces;

import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifier;

public interface DurabilityTracking {
    default void evaluateNewMaxDamage(AbstractModifier.CompiledModifiers<EquipmentModifier> compiledModifiers){}
}
