package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface DamageTracking {

    default float onWearerDamaged(ItemStack stack, LivingEntity wearer, @Nullable LivingEntity attacker, DamageSource source, Float amount){
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        return modifiers.getCompiledData().onDamaged(stack,wearer, attacker,source,amount);
    }
}
