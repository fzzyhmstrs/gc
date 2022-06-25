package me.fzzyhmstrs.amethyst_core.item_util

import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import net.minecraft.util.math.MathHelper

abstract class AbstractScepterItem(material: ToolMaterial, settings: Settings, baseRegen: Int, vararg defaultModifier: Identifier): ToolItem(material, settings), ManaItem {

    private val tickerManaRepair: Long
    private val defaultModifiers: MutableList<Identifier> = mutableListOf()

    init {
        tickerManaRepair = if (material !is ScepterMaterialAddon){
            baseRegen.toLong()
        } else {
            material.healCooldown()
        }
        defaultModifier.forEach {
            defaultModifiers.add(it)
        }
    }

    fun getRepairTime(): Int{
        return tickerManaRepair.toInt()
    }

    override fun isFireproof(): Boolean {
        return true
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        return MathHelper.hsvToRgb(0.66f,1.0f,1.0f)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BLOCK
    }

}