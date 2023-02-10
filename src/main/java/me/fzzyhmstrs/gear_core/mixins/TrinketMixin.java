package me.fzzyhmstrs.gear_core.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import me.fzzyhmstrs.gear_core.interfaces.HitTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Pseudo
@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable, HitTracking, KillTracking, DamageTracking, AttributeTracking {
    @Inject(method = "onEquip", at = @At("TAIL"))
    private void gear_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
            EquipmentModifierHelper.INSTANCE.processModifiers(stack, entity);
    }

    /*@Inject(method = "getModifiers", at = @At("HEAD"))
    private void gear_core_testingTrinkets(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        System.out.println("Trinkets default method called");
        System.out.println(stack.getNbt());
    }*/
    
    /*@ModifyReturnValue(method = "getModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> gear_core_addModifierModifiersToTrinketModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, ItemStack stack,
                                                                                                                SlotReference slot, LivingEntity entity, UUID uuid){
        System.out.println("Trying to get trinket modifiers");
        if (stack.getItem() instanceof AttributeTracking){
            System.out.println("Getting attributes for trinket!" + stack.getTranslationKey());
            return EquipmentModifierHelper.INSTANCE.getAttributeModifiers(stack, original);
        } else {
            return original;
        }
    }*/

    @Override default ModifierInitializer getModifierInitializer(){
        return EquipmentModifierHelper.INSTANCE;
    }

    @Override
    default List<Identifier> defaultModifiers(){return Collections.emptyList();}

    @Override
    default void addModifierTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context){
        EquipmentModifierHelper.INSTANCE.addModifierTooltip(stack, tooltip, context);
    }
}
