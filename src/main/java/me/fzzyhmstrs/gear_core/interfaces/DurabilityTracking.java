package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;

public interface DurabilityTracking {
    default void evaluateNewMaxDamage(AbstractModifier.CompiledModifiers<EquipmentModifier> compiledModifiers){}
}
