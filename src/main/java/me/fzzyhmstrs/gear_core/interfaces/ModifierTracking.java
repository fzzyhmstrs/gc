package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

public interface ModifierTracking {

    default List<Identifier> getModifiers(ItemStack stack){return List.of();}

}
