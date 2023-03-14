package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

public interface ModifierTracking {

    default List<Identifier> getModifiers(ItemStack stack, ModifierHelperType type){return List.of();}

}
