package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Flavorful
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

/**
 * a simple item with the added functionality to display a "flavor text" and optionally a plain text description of what the flavor is depicting.
 *
 * also provides a method for manually setting a glint in an item.
 */
open class CustomFlavorItem(settings: Settings) : Item(settings), Flavorful<CustomFlavorItem> {

    override var glint = false
    override var flavor: String = ""
    override var flavorDesc: String = ""

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        addFlavorText(tooltip, context)
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return if (glint) {
            true
        } else {
            super.hasGlint(stack)
        }
    }

    override fun getFlavorItem(): CustomFlavorItem {
        return this
    }
}