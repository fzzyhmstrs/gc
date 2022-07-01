package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Flavorful
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

open class CustomFlavorToolItem(material: ToolMaterial,settings: Settings) : ToolItem(material, settings), Flavorful<CustomFlavorToolItem> {

    override var glint: Boolean = false
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

    override fun getFlavorItem(): CustomFlavorToolItem {
        return this
    }
}