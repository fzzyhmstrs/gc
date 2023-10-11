package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MineTracking {
    default void postWearerMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity miner){
        /*if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            modifiers.getCompiledData().postMine(stack, world, state, pos, miner);
        }*/
    }
}
