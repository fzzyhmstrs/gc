package me.fzzyhmstrs.amethyst_core.interfaces;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public interface KillTracking {

    default void onWearerKilledOther(ItemStack stack, LivingEntity wearer, LivingEntity victim, ServerWorld world){

    }

    default void incrementKillCount(ItemStack stack){
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("kills")){
            nbt.putInt("kills",1);
        } else {
            int kills = nbt.getInt("kills");
            nbt.putInt("kills",kills + 1);
        }
    }

}
