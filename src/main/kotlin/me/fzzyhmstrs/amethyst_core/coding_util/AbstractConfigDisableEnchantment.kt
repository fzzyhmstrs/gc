package me.fzzyhmstrs.amethyst_core.coding_util

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry


open class AbstractConfigDisableEnchantment(weight: Rarity, target: EnchantmentTarget, vararg slot: EquipmentSlot): Enchantment(weight, target, slot) {

    protected val enabled: Boolean by lazy {
        checkEnabled()
    }

    open fun checkEnabled(): Boolean {
        return true
    }

    fun isEnabled(): Boolean{
        return enabled
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