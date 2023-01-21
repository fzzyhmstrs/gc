package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.fzzyhmstrs.amethyst_core.interfaces.DamageTracking;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketChecker;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LivingEntity.class)
abstract public class LivingEntityMixin {

    @Shadow
    public abstract Iterable<ItemStack> getArmorItems();
    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @ModifyReturnValue(method = "applyEnchantmentsToDamage", at = @At("RETURN"))
    private float amethyst_core_invokeOnWearerDamaged(float original, DamageSource source, float amount){
        if (source.isUnblockable() || original <= 0.0f) return original;
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
