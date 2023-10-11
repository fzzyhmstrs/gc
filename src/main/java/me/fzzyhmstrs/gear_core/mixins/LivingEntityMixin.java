package me.fzzyhmstrs.gear_core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.fzzyhmstrs.gear_core.interfaces.ActiveGearSetsTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import me.fzzyhmstrs.gear_core.set.GearSet;
import me.fzzyhmstrs.gear_core.set.GearSets;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
abstract public class LivingEntityMixin implements ActiveGearSetsTracking {

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();
    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);
 
    @Unique
    private HashMap<GearSet,Integer> gear_core_activeGearSets = new HashMap<>(4,0.75f);
    
    @Override
    public void gear_core_setActiveSets(HashMap<GearSet, Integer> sets){
        gear_core_activeGearSets = sets;
    }

    @Override
    public HashMap<GearSet,Integer> gear_core_getActiveSets(){
        return gear_core_activeGearSets;
    }
    
    @Inject(method = "processEquippedStack", at = @At("HEAD"))
    private void gear_core_processModifiersOnEquip(ItemStack stack, CallbackInfo ci){

    }

    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float gear_core_invokeOnWearerDamaged(float original, DamageSource source, float amount){
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || original <= 0.0f) return original;
        float newAmount = original;
        LivingEntity livingEntity = null;
        if (source.getSource() instanceof LivingEntity le){
            livingEntity = le;
        }
        EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this).getCompiledData().onDamaged(ItemStack.EMPTY,(LivingEntity) (Object) this, livingEntity, source, newAmount);
        /*if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
            List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks((LivingEntity) (Object) this);
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof DamageTracking damageTrackingItem) {
                    newAmount = damageTrackingItem.onWearerDamaged(stack, (LivingEntity) (Object) this, livingEntity, source, newAmount);
                }
            }
        }
        for(ItemStack stack : this.getArmorItems()) {
            if (stack.getItem() instanceof DamageTracking damageTrackingItem){
                newAmount = damageTrackingItem.onWearerDamaged(stack, (LivingEntity) (Object) this, livingEntity,source,newAmount);
            }
        }
        ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof DamageTracking damageTrackingItem){
            newAmount = damageTrackingItem.onWearerDamaged(mainhand, (LivingEntity) (Object) this, livingEntity,source,newAmount);
        }
        ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof DamageTracking damageTrackingItem){
            newAmount = damageTrackingItem.onWearerDamaged(offhand, (LivingEntity) (Object) this, livingEntity,source,newAmount);
        }
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(((StackHolding) this).fzzy_core_getStack())) {
            var innateModifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this);
            newAmount = innateModifiers.getCompiledData().onDamaged(ItemStack.EMPTY, (LivingEntity) (Object) this, livingEntity, source, newAmount);
        }*/
        newAmount = GearSets.INSTANCE.processOnDamaged(newAmount,source,(LivingEntity) (Object) this, livingEntity);
        return newAmount;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void gear_core_invokeTick(CallbackInfo ci){
        EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this).getCompiledData().tick(ItemStack.EMPTY,(LivingEntity) (Object) this, null);
        /*if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
            List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks((LivingEntity) (Object) this);
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof TickTracking tickTrackingItem) {
                    tickTrackingItem.onTick(stack, (LivingEntity) (Object) this, null);
                }
            }
        }
        for(ItemStack stack : this.getArmorItems()) {
            if (stack.getItem() instanceof TickTracking tickTrackingItem){
                tickTrackingItem.onTick(stack, (LivingEntity) (Object) this, null);
            }
        }
        ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof TickTracking tickTrackingItem){
            tickTrackingItem.onTick(mainhand, (LivingEntity) (Object) this, null);
        }
        ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof TickTracking tickTrackingItem){
            tickTrackingItem.onTick(offhand, (LivingEntity) (Object) this, null);
        }
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(((StackHolding) this).fzzy_core_getStack())) {
            var innateModifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this);
            innateModifiers.getCompiledData().tick(ItemStack.EMPTY, (LivingEntity) (Object) this, null);
        }*/
        GearSets.INSTANCE.processTick((LivingEntity) (Object) this);
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "RETURN"))
    private void gear_core_applyGearSetAttributeModifiers(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir){
        if (!(cir.getReturnValue() == null || cir.getReturnValue().isEmpty()))
            GearSets.INSTANCE.updateActiveSets((LivingEntity) (Object) this);
    }
    
}
