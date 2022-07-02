package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.coding_util.Addable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

/**
 * An item or object that extends Modifiable is intended to mesh with the Modifier system. See the [ModifiableScepterItem][me.fzzyhmstrs.amethyst_core.item_util.ModifiableScepterItem] for a default implementation.
 *
 * Description is left intentionally vague as there are any number of directions one can take the Modifier system.
 */

interface Modifiable<T: Addable<T>> {

    val defaultModifiers: MutableList<Identifier>

    fun getActiveModifiers(stack: ItemStack): AbstractModifier<T>.CompiledModifiers
}