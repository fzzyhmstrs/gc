package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.interfaces.DamageTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.KillTracking;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketChecker;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketUtil;
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

    @Inject(method = "onKilledOther", at = @At(value = "HEAD"))
    private void amethyst_core_invokeOnWearerKilledOther(ServerWorld world, LivingEntity livingEntity, CallbackInfo ci){
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
    }

}
