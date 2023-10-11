package me.fzzyhmstrs.gear_core.mixins;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.*;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Pseudo
@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable, HitTracking, KillTracking, DamageTracking, AttributeTracking, TickTracking {

    @Inject(method = "onEquip", at = @At("TAIL"))
    private void gear_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
            EquipmentModifierHelper.INSTANCE.processModifiers(stack, entity);
    }
}
