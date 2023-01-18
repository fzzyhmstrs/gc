package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.interfaces.DamageTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.HitTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.KillTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.MineTracking;
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable;
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifierHelper;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.block.BlockState;
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
public class ArmorItemMixin implements HitTracking, KillTracking, MineTracking, DamageTracking, Modifiable<EquipmentModifier> {

    @Override
    public void postWearerHit(@NotNull ItemStack stack, @NotNull LivingEntity wearer, @NotNull LivingEntity target) {
        AbstractModifier<EquipmentModifier>.CompiledModifiers modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().postHit(stack,wearer,target);
    }

    @Override
    public void onWearerKilledOther(@NotNull ItemStack stack, @NotNull LivingEntity wearer, @NotNull LivingEntity victim, @NotNull ServerWorld world) {
        AbstractModifier<EquipmentModifier>.CompiledModifiers modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().killedOther(stack,wearer,victim);
    }

    @Override
    public void postWearerMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity miner) {
        AbstractModifier<EquipmentModifier>.CompiledModifiers modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().postMine(stack, world, state, pos, miner);
    }

    @Override
    public void onWearerDamaged(ItemStack stack, LivingEntity wearer, @Nullable LivingEntity attacker, DamageSource source, Float amount) {
        AbstractModifier<EquipmentModifier>.CompiledModifiers modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().onDamaged(stack,wearer, attacker,source,amount);
    }

    @NotNull
    @Override
    public List<Identifier> getDefaultModifiers() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public AbstractModifierHelper<EquipmentModifier> getModifierHelper() {
        return EquipmentModifierHelper.INSTANCE;
    }
}
