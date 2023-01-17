package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import net.minecraft.item.ItemStack

object ArmorModifierHelper: AbstractModifierHelper<ArmorModifier>() {

    private val BLANK_ARMOR_MOD = ArmorModifier(ModifierDefaults.BLANK_ID)
    override val fallbackData: AbstractModifier<ArmorModifier>.CompiledModifiers
        get() = BLANK_ARMOR_MOD.CompiledModifiers(listOf(), ArmorModifier(ModifierDefaults.BLANK_ID))

    override fun gatherActiveModifiers(stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            setModifiersById(
                id,
                gatherActiveAbstractModifiers(
                    stack,
                    ModifierDefaults.BLANK_ID,
                    BLANK_ARMOR_MOD.compiler()
                )
            )
        }
    }
}