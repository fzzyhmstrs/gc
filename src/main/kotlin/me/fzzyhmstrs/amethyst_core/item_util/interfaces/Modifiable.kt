package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.coding_util.AcText
import me.fzzyhmstrs.amethyst_core.coding_util.Addable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

/**
 * An item or object that extends Modifiable is intended to mesh with the Modifier system. See the [ModifiableScepterItem][me.fzzyhmstrs.amethyst_core.item_util.ModifiableScepterItem] for a default implementation.
 *
 * Description is left intentionally vague as there are any number of directions one can take the Modifier system.
 */

interface Modifiable<T:AbstractModifier<T>> {

    val defaultModifiers: MutableList<Identifier>

    fun getModifierHelper(): AbstractModifierHelper<T>
}