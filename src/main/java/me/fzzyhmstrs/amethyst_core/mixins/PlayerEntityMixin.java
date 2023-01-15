package me.fzzyhmstrs.amethyst_core.mixins;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.DamageTracking;
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.HitTracking;
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.KillTracking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow public abstract Iterable<ItemStack> getArmorItems();
    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);


    @Inject(method = "attack",at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.postHit (Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void amethyst_core_invokePostWearerHit(Entity target, CallbackInfo ci){
        if (target instanceof LivingEntity livingEntity) {
            Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent((LivingEntity) (Object) this);
            if (optional.isPresent()) {
                List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
                for (Pair<SlotReference, ItemStack> entry : stacks) {
                    if (entry.getRight().getItem() instanceof HitTracking hitTrackingItem) {
                        hitTrackingItem.postWearerHit(entry.getRight(), (LivingEntity) (Object) this, livingEntity);
                    }
                }
            }
            this.getArmorItems().forEach(stack -> {
                if (stack.getItem() instanceof HitTracking hitTrackingItem){
                    hitTrackingItem.postWearerHit(stack, (LivingEntity) (Object) this, livingEntity);
                }
            });
            ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (mainhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(mainhand, (LivingEntity) (Object) this, livingEntity);
            }
            ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
            if (offhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(offhand, (LivingEntity) (Object) this, livingEntity);
            }
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "net/minecraft/entity/damage/DamageTracker.onDamage (Lnet/minecraft/entity/damage/DamageSource;FF)V"))
    private void amethyst_core_invokeOnWearerDamaged(DamageSource source, float amount, CallbackInfo ci){
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent((LivingEntity) (Object) this);
        LivingEntity livingEntity = null;
        if (source.getSource() instanceof LivingEntity le){
            livingEntity = le;
        }
        if (optional.isPresent()) {
            List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
            for (Pair<SlotReference, ItemStack> entry : stacks) {
                if (entry.getRight().getItem() instanceof DamageTracking damageTrackingItem) {
                    damageTrackingItem.onWearerDamaged(entry.getRight(), (LivingEntity) (Object) this, livingEntity, source, amount);
                }
            }
        }
        for(ItemStack stack : this.getArmorItems()) {
            if (stack.getItem() instanceof DamageTracking damageTrackingItem){
                damageTrackingItem.onWearerDamaged(stack, (LivingEntity) (Object) this, livingEntity,source,amount);
            }
        }
        ItemStack mainhand = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof DamageTracking damageTrackingItem){
            damageTrackingItem.onWearerDamaged(mainhand, (LivingEntity) (Object) this, livingEntity,source,amount);
        }
        ItemStack offhand = this.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof DamageTracking damageTrackingItem){
            damageTrackingItem.onWearerDamaged(offhand, (LivingEntity) (Object) this, livingEntity,source,amount);
        }
    }

    @Inject(method = "onKilledOther", at = @At(value = "HEAD"))
    private void amethyst_core_invokeOnWearerKilledOther(ServerWorld world, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir){
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent((LivingEntity) (Object) this);
        if (optional.isPresent()) {
            List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
            for (Pair<SlotReference, ItemStack> entry : stacks) {
                if (entry.getRight().getItem() instanceof KillTracking killTrackingItem) {
                    killTrackingItem.onWearerKilledOther(entry.getRight(), (LivingEntity) (Object) this, livingEntity, world);
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
    }

}
