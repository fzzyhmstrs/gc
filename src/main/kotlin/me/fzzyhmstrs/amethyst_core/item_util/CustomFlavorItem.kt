package me.fzzyhmstrs.amethyst_core.item_util

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.World

open class CustomFlavorItem(settings: Settings, private val glint: Boolean, private val id: Identifier) : Item(settings) {
    
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip.add(TranslatableText("item.${id.namespace}.${id.path}.tooltip1").formatted(Formatting.WHITE, Formatting.ITALIC))
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return if (glint) {
            true
        } else {
            super.hasGlint(stack)
        }
    }
}