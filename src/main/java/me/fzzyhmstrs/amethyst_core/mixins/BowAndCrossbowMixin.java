package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.interfaces.HitTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.KillTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.Modifiable;
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierInitializer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin({BowItem.class, CrossbowItem.class})
public class BowAndCrossbowMixin implements HitTracking, KillTracking, Modifiable {

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
