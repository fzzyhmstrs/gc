package me.fzzyhmstrs.amethyst_core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface UseTracking {

    default void onWearerUse(ItemStack stack, World world, PlayerEntity user, Hand hand){

    }
}
