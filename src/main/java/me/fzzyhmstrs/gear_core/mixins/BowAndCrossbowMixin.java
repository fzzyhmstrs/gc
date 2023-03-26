package me.fzzyhmstrs.gear_core.mixins;

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer;
import me.fzzyhmstrs.gear_core.interfaces.HitTracking;
import me.fzzyhmstrs.gear_core.interfaces.KillTracking;
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin({BowItem.class, CrossbowItem.class})
public class BowAndCrossbowMixin implements HitTracking, KillTracking, AttributeTracking, Modifiable {

    @Override 
    public boolean correctSlot(EquipmentSlot slot){
        return slot == EquipmentSlot.MAINHAND;
    }
    
    @Override
    public EquipmentSlot getCorrectSlot(){
        return EquipmentSlot.MAINHAND;
    }
    
    @NotNull
    @Override
    public List<Identifier> defaultModifiers(ModifierHelperType type) {
        return Collections.emptyList();
    }
}
