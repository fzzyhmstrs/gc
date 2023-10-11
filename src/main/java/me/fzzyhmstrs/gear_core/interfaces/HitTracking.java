package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface HitTracking {
    default void postWearerHit(ItemStack stack, LivingEntity wearer, LivingEntity target){
        /*if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            modifiers.getCompiledData().postHit(stack, wearer, target);
        }*/
    }
}
