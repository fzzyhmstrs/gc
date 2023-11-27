package me.fzzyhmstrs.gear_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker;
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import me.fzzyhmstrs.gear_core.set.GearSets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract Iterable<ItemStack> getArmorItems();
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Inject(method = "onKilledOther", at = @At(value = "HEAD"))
    private void gear_core_invokeOnWearerKilledOther(ServerWorld world, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir){
        EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this).getCompiledData().killedOther(((LivingEntity) (Object) this).getEquippedStack(EquipmentSlot.MAINHAND),(LivingEntity) (Object) this,livingEntity);
        if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
            List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks((LivingEntity) (Object) this);
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof KillTracking killTrackingItem) {
                    killTrackingItem.onWearerKilledOther(stack, (LivingEntity) (Object) this, livingEntity, world);
                }
            }
        }
        for(ItemStack stack : this.getArmorItems()) {
            if (stack.getItem() instanceof KillTracking killTrackingItem){
                killTrackingItem.onWearerKilledOther(stack, (LivingEntity) (Object) this, livingEntity, world);
            }
        }
        ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof KillTracking killTrackingItem){
            killTrackingItem.onWearerKilledOther(mainhand, (LivingEntity) (Object) this, livingEntity, world);
        }
        ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof KillTracking killTrackingItem){
            killTrackingItem.onWearerKilledOther(offhand, (LivingEntity) (Object) this, livingEntity, world);
        }
        GearSets.INSTANCE.processOnKilledOther((PlayerEntity) (Object) this, livingEntity);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.damage (Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean gear_core_modifyAttackDamage(Entity instance, DamageSource source, float damage, Operation<Boolean> operation){
        if (!(instance instanceof LivingEntity)) return operation.call(instance,source,damage);
        float newAmount = damage;
        newAmount = EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this).getCompiledData().onAttack(((LivingEntity) (Object) this).getEquippedStack(EquipmentSlot.MAINHAND),(LivingEntity) (Object) this,(LivingEntity) instance, source, newAmount);
        /*if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
            List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks((LivingEntity) (Object) this);
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof DamageTracking damageTracking) {
                    newAmount = damageTracking.onAttack(stack, (LivingEntity) (Object) this, (LivingEntity) instance,source,newAmount);
                }
            }
        }
        for(ItemStack stack : this.getArmorItems()) {
            if (stack.getItem() instanceof DamageTracking damageTracking) {
                newAmount = damageTracking.onAttack(stack, (LivingEntity) (Object) this, (LivingEntity) instance, source, newAmount);
            }
        }
        ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof DamageTracking damageTracking){
            newAmount = damageTracking.onAttack(mainhand, (LivingEntity) (Object) this, (LivingEntity) instance, source, newAmount);
        }
        ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof DamageTracking damageTracking){
            newAmount = damageTracking.onAttack(offhand, (LivingEntity) (Object) this, (LivingEntity) instance, source, newAmount);
        }
        if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(((StackHolding) this).fzzy_core_getStack())) {
            var innateModifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers((LivingEntity) (Object) this);
            newAmount = innateModifiers.getCompiledData().onAttack(ItemStack.EMPTY, (LivingEntity) (Object) this, (LivingEntity) instance, source, newAmount);
        }*/
        newAmount = GearSets.INSTANCE.processOnAttack(newAmount,source,(LivingEntity) (Object) this,(LivingEntity) instance);
        return operation.call(instance,source,newAmount);
    }

}
