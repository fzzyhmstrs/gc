package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.coding_util.Addable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

/**
 * An item or object that extends Modifiable is intended to mesh with the Modifier system. See the [ModifiableScepterItem][me.fzzyhmstrs.amethyst_core.item_util.ModifiableScepterItem] for a default implementation.
 *
 * Description is left intentionally vague as there are any number of directions one can take the Modifier system.
 */

interface Modifiable<T: Addable<T>> {

    val defaultModifiers: MutableList<Identifier>

    fun getActiveModifiers(stack: ItemStack): AbstractModifier<T>.CompiledModifiers

    fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>){
        val commaText: MutableText = LiteralText(", ").formatted(Formatting.GOLD)
        val modifierList = ModifierHelper.getModifiers(stack)
        if (modifierList.isNotEmpty()){
            val modifierText = TranslatableText("scepter.modifiers").formatted(Formatting.GOLD)
            val itr = modifierList.asIterable().iterator()
            while(itr.hasNext()){
                val mod = itr.next()
                modifierText.append(TranslatableText("scepter.modifier.${mod}").formatted(Formatting.GOLD))
                if (itr.hasNext()){
                    modifierText.append(commaText)
                }
            }
            tooltip.add(modifierText)
        }
    }
}