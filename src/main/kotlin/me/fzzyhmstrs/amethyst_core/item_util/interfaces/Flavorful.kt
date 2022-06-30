package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

interface Flavorful<T: Flavorful<T>> {

    var glint: Boolean
    var flavor: String

    fun withFlavor(flavorPath: String): T {
        getFlavorItem().flavor = flavorPath
        return getFlavorItem()
    }

    fun withFlavorDefaultPath(id: Identifier): T {
        getFlavorItem().flavor = "item.${id.namespace}.${id.path}.tooltip1"
        return getFlavorItem()
    }

    fun withGlint(): T {
        getFlavorItem().glint = true
        return getFlavorItem()
    }

    fun flavorText(): MutableText{
        return TranslatableText(flavor).formatted(Formatting.WHITE, Formatting.ITALIC)
    }

    fun getFlavorItem():T
}