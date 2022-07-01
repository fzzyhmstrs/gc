package me.fzzyhmstrs.amethyst_core.item_util.interfaces

import me.fzzyhmstrs.amethyst_core.config.AcConfig
import net.minecraft.client.item.TooltipContext
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

interface Flavorful<T: Flavorful<T>> {

    var glint: Boolean
    var flavor: String
    var flavorDesc: String

    fun withFlavor(flavorPath: String): T {
        getFlavorItem().flavor = flavorPath
        return getFlavorItem()
    }

    fun withFlavorDesc(flavorPath: String): T{
        getFlavorItem().flavorDesc = flavorPath
        return getFlavorItem()
    }

    fun withFlavorDefaultPath(id: Identifier): T {
        getFlavorItem().flavor = "item.${id.namespace}.${id.path}.flavor"
        return getFlavorItem()
    }

    fun withFlavorDescDefaultPath(id: Identifier): T {
        getFlavorItem().flavorDesc = "item.${id.namespace}.${id.path}.flavor.desc"
        return getFlavorItem()
    }

    fun withGlint(): T {
        getFlavorItem().glint = true
        return getFlavorItem()
    }

    fun flavorText(): MutableText{
        return TranslatableText(flavor).formatted(Formatting.WHITE, Formatting.ITALIC)
    }
    fun flavorDescText(): MutableText{
        return TranslatableText(flavorDesc).formatted(Formatting.WHITE)
    }

    fun addFlavorText(tooltip: MutableList<Text>, context: TooltipContext){
        if (flavor != "") {
            tooltip.add(flavorText())
        }
        if ((context.isAdvanced && AcConfig.flavors.showFlavorDescOnAdvanced) || AcConfig.flavors.showFlavorDesc){
            if (flavorDesc != ""){
                tooltip.add(flavorDescText())
            }
        }
    }

    fun getFlavorItem():T
}