package me.fzzyhmstrs.amethyst_core.mixins;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import me.fzzyhmstrs.amethyst_core.interfaces.Modifiable;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Trinket.class)
public interface TrinketMixin extends Modifiable {

    @Inject(method = "onEquip", at = @At("TAIL"))
    private void amethyst_core_processOnEquipForAugments(ItemStack stack, SlotReference slot, LivingEntity entity, CallbackInfo ci){
        EquipmentModifierHelper.INSTANCE.processEquipmentAugmentModifiers(stack,entity);
    }


}