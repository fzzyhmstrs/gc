package me.fzzyhmstrs.gear_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    /*@WrapOperation(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", target = "net/minecraft/screen/PlayerScreenHandler.setStackInSlot (IILnet/minecraft/item/ItemStack;)V"))
    private void gear_core_passModifiersToClient(PlayerScreenHandler instance, int i, int revision, ItemStack stack, Operation<Void> operation){
        if (stack.getItem() instanceof Modifiable modifiable){
            System.out.println("trying to fix client-side modifiers");
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains(NbtKeys.MODIFIERS.str())) {
                modifiable.getModifierInitializer().initializeModifiers(stack, nbt, List.of());
            }
        }
        operation.call(instance,i,revision,stack);
    }*/

}
