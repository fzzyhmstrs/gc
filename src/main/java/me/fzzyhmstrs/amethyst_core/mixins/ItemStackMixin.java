package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.fzzyhmstrs.amethyst_core.interfaces.HitTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.MineTracking;
import me.fzzyhmstrs.amethyst_core.interfaces.UseTracking;
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    
    @Inject(method = "postHit", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void amethyst_core_invokePostWearerHit(LivingEntity target, PlayerEntity attacker, CallbackInfo ci){
            Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(attacker);
            if (optional.isPresent()) {
                List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
                for (Pair<SlotReference, ItemStack> entry : stacks) {
                    if (entry.getRight().getItem() instanceof HitTracking hitTrackingItem) {
                        hitTrackingItem.postWearerHit(entry.getRight(), attacker, target);
                    }
                }
            }
            attacker.getArmorItems().forEach(stack -> {
                if (stack.getItem() instanceof HitTracking hitTrackingItem){
                    hitTrackingItem.postWearerHit(stack, attacker, target);
                }
            });
            ItemStack mainhand = attacker.getEquippedStack(EquipmentSlot.MAINHAND);
            if (mainhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(mainhand, attacker, target);
            }
            ItemStack offhand = attacker.getEquippedStack(EquipmentSlot.OFFHAND);
            if (offhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(offhand, attacker, target);
            }
    }

    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void amethyst_core_invokePostWearerMine(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci){
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(miner);
        if (optional.isPresent()) {
            List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
            for (Pair<SlotReference, ItemStack> entry : stacks) {
                if (entry.getRight().getItem() instanceof MineTracking mineTrackingItem) {
                    mineTrackingItem.postWearerMine(entry.getRight(), world,state,pos,miner);
                }
            }
        }
        miner.getArmorItems().forEach(stack -> {
            if (stack.getItem() instanceof MineTracking mineTrackingItem) {
                mineTrackingItem.postWearerMine(stack, world,state,pos,miner);
            }
        });
        ItemStack mainhand = miner.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof MineTracking mineTrackingItem) {
            mineTrackingItem.postWearerMine(mainhand, world,state,pos,miner);
        }
        ItemStack offhand = miner.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof MineTracking mineTrackingItem) {
            mineTrackingItem.postWearerMine(offhand, world,state,pos,miner);
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.use (Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> amethyst_core_invokeOnWearerUse(World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> operation){
        TypedActionResult<ItemStack> useResult = operation.call(world, user, hand);
        if (!useResult.getResult().isAccepted()){
            Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(user);
            if (optional.isPresent()) {
                List<Pair<SlotReference, ItemStack>> stacks = optional.get().getAllEquipped();
                for (Pair<SlotReference, ItemStack> entry : stacks) {
                    if (entry.getRight().getItem() instanceof UseTracking useTrackingItem) {
                        useTrackingItem.onWearerUse(user.getStackInHand(hand), world, user, hand);
                    }
                }
            }
            user.getArmorItems().forEach(stack -> {
                if (stack.getItem() instanceof UseTracking useTrackingItem) {
                    useTrackingItem.onWearerUse(user.getStackInHand(hand), world, user, hand);
                }
            });
            ItemStack mainhand = user.getEquippedStack(EquipmentSlot.MAINHAND);
            if (mainhand.getItem() instanceof UseTracking useTrackingItem) {
                useTrackingItem.onWearerUse(user.getStackInHand(hand), world, user, hand);
            }
            ItemStack offhand = user.getEquippedStack(EquipmentSlot.OFFHAND);
            if (offhand.getItem() instanceof UseTracking useTrackingItem) {
                useTrackingItem.onWearerUse(user.getStackInHand(hand), world, user, hand);
            }
        }
        return useResult;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.appendTooltip (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V"))
    private void amethyst_core_appendModifiersToTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, Operation<Void> operation){
        operation.call(stack, world, tooltip,context);
        if (stack.getItem() instanceof Modifiable modifiable){
            modifiable.getModifierHelper().addModifierTooltip(stack, tooltip);
        }
    }

}
