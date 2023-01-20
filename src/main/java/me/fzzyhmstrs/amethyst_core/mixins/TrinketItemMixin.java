package me.fzzyhmstrs.amethyst_core.mixins;

import dev.emi.trinkets.api.TrinketItem;
import me.fzzyhmstrs.amethyst_core.interfaces.AugmentTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.DamageTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.HitTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.KillTracking;
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

@Pseudo
@Mixin(TrinketItem.class)
public class TrinketItemMixin implements AugmentTracking, HitTracking, KillTracking, DamageTracking {

    @Override
    public float onWearerDamaged(ItemStack stack, LivingEntity wearer, @Nullable LivingEntity attacker, DamageSource source, Float amount) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        return modifiers.getCompiledData().onDamaged(stack,wearer, attacker,source,amount);
    }

    @Override
    public List<AugmentModifier> getModifiers(ItemStack stack) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        return modifiers.getCompiledData().augmentModifiers();
    }

    @Override
    public void postWearerHit(@NotNull ItemStack stack, @NotNull LivingEntity wearer, @NotNull LivingEntity target) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().postHit(stack,wearer,target);
    }

    @Override
    public void onWearerKilledOther(@NotNull ItemStack stack, @NotNull LivingEntity wearer, @NotNull LivingEntity victim, @NotNull ServerWorld world) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().killedOther(stack,wearer,victim);
    }


}
