package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.coding_util.Addable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

interface Modifiable<T: Addable<T>> {

    val defaultModifiers: MutableList<Identifier>

    fun getActiveModifiers(stack: ItemStack): AbstractModifier<T>.CompiledModifiers
}