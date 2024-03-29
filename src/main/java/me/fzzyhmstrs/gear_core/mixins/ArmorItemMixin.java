package me.fzzyhmstrs.gear_core.mixins;

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.GC;
import me.fzzyhmstrs.gear_core.interfaces.*;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin implements HitTracking, KillTracking, MineTracking, DamageTracking, ModifierTracking, AttributeTracking,TickTracking, Modifiable {

    @Shadow
    public abstract EquipmentSlot getSlotType();
    
    @Override
    public boolean fzzy_core_correctSlot(EquipmentSlot slot){
        return getSlotType() == slot;
    }
    
    @Override
    public EquipmentSlot fzzy_core_getCorrectSlot(){
        return getSlotType();
    }
    
/*    @Override
    public List<Identifier> getModifiers(ItemStack stack, ModifierHelperType type) {
        *//*if (EquipmentModifierHelper.INSTANCE.hasActiveModifiers(stack)) {
            AbstractModifier.CompiledModifiers<EquipmentModifier> modifiers = EquipmentModifierHelper.INSTANCE.getActiveModifiers(stack);
            return modifiers.getCompiledData().modifiers();
        } else {
            return List.of();
        }*//*
    }*/

    @NotNull
    @Override
    public List<Identifier> defaultModifiers(ModifierHelperType type) {
        return Collections.emptyList();
    }
}
