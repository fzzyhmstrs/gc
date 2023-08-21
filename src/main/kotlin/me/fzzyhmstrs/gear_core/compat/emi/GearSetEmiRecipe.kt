package me.fzzyhmstrs.gear_core.compat.emi

import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import me.fzzyhmstrs.gear_core.set.GearSet
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.Arrays

class GearSetEmiRecipe(private val id: Identifier, set: GearSet): EmiRecipe {

    private val items = Arrays.stream(set.getStacks()).map { EmiStack.of(it) }.toList()
    private val tooltip: List<OrderedText>

    init{
        val list = mutableListOf<Text>()
        set.appendTooltip(0, ItemStack.EMPTY, TooltipContext.Default.NORMAL, list)
        val list2 = mutableListOf<OrderedText>()
        for (tip in list){
            list2 .addAll(MinecraftClient.getInstance().textRenderer.wrapLines(tip,160))
        }
        tooltip = list2
    }

    override fun getCategory(): EmiRecipeCategory {
        return EmiClientPlugin.GEAR_SET_CATEGORY
    }

    override fun getId(): Identifier{
        return id
    }

    override fun getInputs(): List<EmiIngredient>{
        return items
    }

    override fun getOutputs(): List<EmiStack>{
        return items
    }

    override fun getDisplayWidth(): Int{
        return 160
    }

    override fun getDisplayHeight(): Int{
        return tooltip.size * 10 + 10 + ((items.size - 1)/8 + 1) * 18
    }

    override fun supportsRecipeTree(): Boolean {
        return false
    }

    override fun addWidgets(widgets: WidgetHolder) {
        var xOffset = 0
        var yOffset = 0
        for (tip in tooltip) {
            widgets.addText(tip,xOffset,yOffset,0xFFFFFF,false)
            yOffset += 10
        }
        yOffset += 10
        items.forEachIndexed { index, emiStack ->
            xOffset = (index % 8) * 18
            val yOffset2 = (index / 8) * 18
            widgets.addSlot(emiStack,xOffset,yOffset + yOffset2)
        }

    }


}