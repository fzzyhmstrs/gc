package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

interface Flavorful {

    var glint: Boolean
    var flavor: String

    fun withFlavor(flavorPath: String): Flavorful {
        flavor = flavorPath
        return this
    }

    fun withFlavorDefaultPath(id: Identifier): Flavorful {
        flavor = "item.${id.namespace}.${id.path}.tooltip1"
        return this
    }

    fun withGlint(): Flavorful {
        glint = true
        return this
    }

    fun flavorText(): MutableText{
        return TranslatableText(flavor).formatted(Formatting.WHITE, Formatting.ITALIC)
    }

}