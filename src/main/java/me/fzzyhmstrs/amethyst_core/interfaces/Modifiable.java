package me.fzzyhmstrs.amethyst_core.interfaces;


import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifierHelper;
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierInitializer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public interface Modifiable {

    default List<Identifier> defaultModifiers(){return Collections.emptyList();}

    default void addModifierTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context){
        AbstractModifierHelper.Companion.getEmptyHelper().addModifierTooltip(stack, tooltip, context);
    }

    default ModifierInitializer getModifierInitializer(){
        return AbstractModifierHelper.Companion.getEmptyHelper();
    }
}
