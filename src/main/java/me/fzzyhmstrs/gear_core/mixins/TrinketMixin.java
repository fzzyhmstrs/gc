package me.fzzyhmstrs.gear_core.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import me.fzzyhmstrs.gear_core.interfaces.HitTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Pseudo
@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable, HitTracking, KillTracking, DamageTracking {
    @Inject(method = "onEquip", at = @At("TAIL"))
    private void gear_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
            EquipmentModifierHelper.INSTANCE.processModifiers(stack, entity);
    }

    @ModifyReturnValue(method = "getModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> gear_core_addModifierModifiersToTrinketModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, ItemStack stack,
                                                                                                                SlotReference slot, LivingEntity entity, UUID uuid){
        if (stack.getItem() instanceof AttributeTracking){
            return EquipmentModifierHelper.INSTANCE.getAttributeModifiers(stack, original);
        } else {
            return original;
        }
    }
}
