package me.fzzyhmstrs.gear_core.mixins;

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.*;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin({ToolItem.class, TridentItem.class})
public class ToolAndWeaponItemMixin implements HitTracking, KillTracking, MineTracking, UseTracking, Modifiable {

    @Override
    public void onWearerUse(ItemStack stack, World world, PlayerEntity user, Hand hand) {
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().onUse(stack,user,null);
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
