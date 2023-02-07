package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface HitTracking {
    default void postWearerHit(ItemStack stack, LivingEntity wearer, LivingEntity target){
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().postHit(stack,wearer,target);
    }
}
