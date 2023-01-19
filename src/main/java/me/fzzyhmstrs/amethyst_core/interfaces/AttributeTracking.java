package me.fzzyhmstrs.amethyst_core.interfaces;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

public interface AttributeTracking {

    default Multimap<EntityAttribute, EntityAttributeModifier> getModifierAttributeModifiers(ItemStack stack){
        return HashMultimap.create();
    }
}
