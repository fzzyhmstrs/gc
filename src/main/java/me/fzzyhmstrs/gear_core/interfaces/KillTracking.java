package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public interface KillTracking {

    default void onWearerKilledOther(ItemStack stack, LivingEntity wearer, LivingEntity victim, ServerWorld world){
        AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
        modifiers.getCompiledData().killedOther(stack,wearer,victim);
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

    default int getKillCount(ItemStack stack){
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return 0;
        if (!nbt.contains("kills")){
            return 0;
        } else {
            return nbt.getInt("kills");
        }
    }

}
