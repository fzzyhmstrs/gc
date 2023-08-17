package me.fzzyhmstrs.gear_core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker;
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntity.class)
abstract public class LivingEntityMixin implements ActiveGearSetTracking {

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();
    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);
 
    @Unique
    private Set<GearSet> gear_core_activeGearSets = new HashSet(4,0.75f);
    
    @Override
    void setActiveSets(Set<GearSet> sets){
        gear_core_activeGearSets = sets;
    }
    Set<GearSet> getActiveSets(){
        return gear_core_activeGearSets;
    }
    
    @Inject(method = "processEquippedStack", at = @At("HEAD"))
    private void gear_core_processModifiersOnEquip(ItemStack stack, CallbackInfo ci){

    }

    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float gear_core_invokeOnWearerDamaged(float original, DamageSource source, float amount){
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || original <= 0.0f) return original;
        float newAmount = original;
        LivingEntity livingEntity = null;
        if (source.getSource() instanceof LivingEntity le){
            livingEntity = le;
        }
        if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
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
        return newAmount;
    }

}
