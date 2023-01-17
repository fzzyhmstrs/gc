package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import net.minecraft.item.ItemStack

object ToolModifierHelper: AbstractModifierHelper<ToolModifier>() {

    private val BLANK_WEAPON_MOD = ToolModifier(ModifierDefaults.BLANK_ID)
    override val fallbackData: AbstractModifier<ToolModifier>.CompiledModifiers
        get() = BLANK_WEAPON_MOD.CompiledModifiers(listOf(), ToolModifier(ModifierDefaults.BLANK_ID))

    override fun gatherActiveModifiers(stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            setModifiersById(
                id,
                gatherActiveAbstractModifiers(
                    stack,
                    ModifierDefaults.BLANK_ID,
                    BLANK_WEAPON_MOD.compiler()
                )
            )
        }
    }
}