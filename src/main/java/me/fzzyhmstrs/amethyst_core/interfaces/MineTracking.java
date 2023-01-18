package me.fzzyhmstrs.amethyst_core.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MineTracking {
    default void postWearerMine(ItemStack stack, World world, BlockState state, BlockPos pos, PlayerEntity miner){

    }
}
