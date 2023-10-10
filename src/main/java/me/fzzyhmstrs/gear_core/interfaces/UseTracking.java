package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface UseTracking {

    default void onWearerUse(ItemStack stack, World world, PlayerEntity user, Hand hand){
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            modifiers.getCompiledData().onUse(stack, user, null);
        }
    }
}
