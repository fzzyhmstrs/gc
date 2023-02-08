package me.fzzyhmstrs.gear_core.mixins;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import me.fzzyhmstrs.gear_core.interfaces.HitTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable, HitTracking, KillTracking, DamageTracking {
    @Inject(method = "onEquip", at = @At("TAIL"))
    private void gear_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
            EquipmentModifierHelper.INSTANCE.processModifiers(stack, entity);
    }
}
