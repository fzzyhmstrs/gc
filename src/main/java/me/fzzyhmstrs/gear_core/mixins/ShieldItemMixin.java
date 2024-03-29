package me.fzzyhmstrs.gear_core.mixins;

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.*;
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
public class ShieldItemMixin implements HitTracking, KillTracking, DamageTracking, AttributeTracking, TickTracking, Modifiable {
    
    @Override
    public boolean fzzy_core_correctSlot(EquipmentSlot slot){
        return slot == EquipmentSlot.OFFHAND;
    }
    
    @Override
    public EquipmentSlot fzzy_core_getCorrectSlot(){
        return EquipmentSlot.OFFHAND;
    }

    @NotNull
    @Override
    public List<Identifier> defaultModifiers(ModifierHelperType type) {
        return Collections.emptyList();
    }
}
