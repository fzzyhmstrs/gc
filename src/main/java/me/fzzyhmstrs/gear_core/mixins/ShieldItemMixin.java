package me.fzzyhmstrs.gear_core.mixins;

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking;
import me.fzzyhmstrs.gear_core.interfaces.DamageTracking;
import me.fzzyhmstrs.gear_core.interfaces.HitTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(ShieldItem.class)
public class ShieldItemMixin implements HitTracking, KillTracking, DamageTracking, AttributeTracking, Modifiable {
    
    @Override
    public boolean correctSlot(EquipmentSlot slot){
        return slot == EquipmentSlot.OFFHAND;
    }
    
    @Override
    public EquipmentSlot getCorrectSlot(){
        return EquipmentSlot.OFFHAND;
    }

    @NotNull
    @Override
    public List<Identifier> defaultModifiers() {
        return Collections.emptyList();
    }

    @Override
    public void addModifierTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context) {
        EquipmentModifierHelper.INSTANCE.addModifierTooltip(stack, tooltip, context);
    }

    @Override
    public ModifierInitializer getModifierInitializer() {
        return EquipmentModifierHelper.INSTANCE;
    }
}
