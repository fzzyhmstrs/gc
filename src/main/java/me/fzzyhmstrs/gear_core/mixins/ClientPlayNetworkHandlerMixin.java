package me.fzzyhmstrs.gear_core.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

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
