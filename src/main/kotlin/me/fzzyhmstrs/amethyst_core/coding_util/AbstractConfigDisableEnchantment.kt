package me.fzzyhmstrs.amethyst_core.coding_util

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


open class AbstractConfigDisableEnchantment(weight: Rarity, target: EnchantmentTarget, vararg slot: EquipmentSlot): Enchantment(weight, target, slot) {

    protected var enabled: Boolean = checkEnabledInternal()

    private fun checkEnabledInternal(): Boolean{
        return checkEnabled()
    }

    open fun checkEnabled(): Boolean {
        return true
    }

    fun isEnabled(): Boolean{
        return enabled
    }

    fun updateEnabled(){
        enabled = checkEnabled()
    }

    override fun getName(level: Int): Text {
        val baseText = super.getName(level) as MutableText
        if (!enabled) {
            return baseText
                .append(AcText.translatable("scepter.augment.disabled"))
                .formatted(Formatting.DARK_RED)
                .formatted(Formatting.STRIKETHROUGH)
        }
        return baseText
    }
}