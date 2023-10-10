package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface DamageTracking {

    default float onAttack(ItemStack stack, LivingEntity wearer, @Nullable LivingEntity attacker, DamageSource source, Float amount){
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            return modifiers.getCompiledData().onAttack(stack, wearer, attacker, source, amount);
        } else {
            return amount;
        }
    }

    default float onWearerDamaged(ItemStack stack, LivingEntity wearer, @Nullable LivingEntity attacker, DamageSource source, Float amount){
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            return modifiers.getCompiledData().onDamaged(stack, wearer, attacker, source, amount);
        } else {
            return amount;
        }
    }
}
