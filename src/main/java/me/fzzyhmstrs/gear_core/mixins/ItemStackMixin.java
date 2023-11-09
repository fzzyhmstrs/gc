package me.fzzyhmstrs.gear_core.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.gear_core.GC;
import me.fzzyhmstrs.gear_core.interfaces.*;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import me.fzzyhmstrs.gear_core.set.GearSets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DurabilityTracking {

    @Shadow public abstract Item getItem();

    @Shadow public abstract String toString();

    @Unique
    private int gear_core_newMaxDamage;

    @Override
    public void gear_core_evaluateNewMaxDamage(AbstractModifier.CompiledModifiers<EquipmentModifier> compiledModifiers) {
        if (getItem().getMaxDamage() != 0) {
            gear_core_newMaxDamage = Math.max(compiledModifiers.getCompiledData().modifyDurability(getItem().getMaxDamage()), 1);
        }
    }

    @WrapOperation(method = "getMaxDamage", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.getMaxDamage ()I"))
    private int gear_core_maxDamageFromNewMax(Item instance, Operation<Integer> operation){
        int original = operation.call(instance);
        if (original != 0 && gear_core_newMaxDamage == 0){
            Item item = getItem();
            if (item instanceof Modifiable modifiable){
                if (modifiable.canBeModifiedBy(GC.INSTANCE.getEQUIPMENT_MODIFIER_TYPE()))
                    GC.INSTANCE.getEQUIPMENT_MODIFIER_TYPE().getModifierInitializer().initializeModifiers((ItemStack) (Object) this);
            }
            if (gear_core_newMaxDamage == 0){
                gear_core_newMaxDamage = original;
            }
        }
        return gear_core_newMaxDamage;
    }

    /*@ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> gear_core_addModifierModifiersToModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, EquipmentSlot slot){
        if (getItem() instanceof AttributeTracking at && !at.fzzy_core_correctSlot(slot)){
            return original;
        } else if (getItem() instanceof AttributeTracking){
            return EquipmentModifierHelper.INSTANCE.getAttributeModifiers((ItemStack) (Object) this, original);
        } else {
            return original;
        }
    }*/

    @Inject(method = "postHit", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void gear_core_invokePostWearerHit(LivingEntity target, PlayerEntity attacker, CallbackInfo ci){
        EquipmentModifierHelper.INSTANCE.getActiveModifiers(attacker).getCompiledData().postHit(attacker.getEquippedStack(EquipmentSlot.MAINHAND),attacker,target);
        GearSets.INSTANCE.processPostHit(target, attacker);
    }

    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void gear_core_invokePostWearerMine(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci){
        EquipmentModifierHelper.INSTANCE.getActiveModifiers(miner).getCompiledData().postMine(miner.getEquippedStack(EquipmentSlot.MAINHAND), world, state, pos, miner);
        GearSets.INSTANCE.processPostMine(world, state, pos, miner);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.use (Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> gear_core_invokeOnWearerUse(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> operation){
        TypedActionResult<ItemStack> useResult = operation.call(instance, world, user, hand);
        if (!useResult.getResult().isAccepted()){
            EquipmentModifierHelper.INSTANCE.getActiveModifiers(user).getCompiledData().onUse(user.getStackInHand(hand),user,null);
            GearSets.INSTANCE.processOnUse(hand, user);
        }
        return useResult;
    }
}
