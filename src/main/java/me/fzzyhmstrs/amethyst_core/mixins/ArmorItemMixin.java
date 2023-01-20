package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.interfaces.*;
import me.fzzyhmstrs.amethyst_core.modifier_util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(ArmorItem.class)
public class ArmorItemMixin implements HitTracking, KillTracking, MineTracking, DamageTracking, AugmentTracking, Modifiable {

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

    @Override
    public void postWearerMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity miner) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().postMine(stack, world, state, pos, miner);
    }

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

    @NotNull
    @Override
    public List<Identifier> defaultModifiers() {
        return Collections.emptyList();
    }

    @Override
    public void addModifierTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context) {
        EquipmentModifierHelper.INSTANCE.addModifierTooltip(stack, tooltip, context);
    }

    @Override
    public ModifierInitializer getModifierInitializer() {
        return EquipmentModifierHelper.INSTANCE;
    }
}
