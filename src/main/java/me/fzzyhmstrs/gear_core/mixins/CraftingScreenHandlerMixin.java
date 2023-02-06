package me.fzzyhmstrs.gear_core.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Debug(export = true)
@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Redirect(method = "method_17400", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.onCraft (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private static void gear_core_changeItemOnCraftToStackOnCraft(Item instance, ItemStack stack, World world, PlayerEntity player){
        stack.onCraft(world,player, stack.getCount());
    }

}
