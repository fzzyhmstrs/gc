package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface HitTracking {
    default void postWearerHit(ItemStack stack, LivingEntity wearer, LivingEntity target){

    }
}
