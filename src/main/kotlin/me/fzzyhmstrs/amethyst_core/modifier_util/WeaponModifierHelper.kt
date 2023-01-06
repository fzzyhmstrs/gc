package me.fzzyhmstrs.viscerae.modifier

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierDefaults
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object WeaponModifierHelper: AbstractModifierHelper<WeaponModifier>() {

    private val BLANK_WEAPON_MOD = WeaponModifier(ModifierDefaults.BLANK_ID)
    override val fallbackData: AbstractModifier<WeaponModifier>.CompiledModifiers
        get() = BLANK_WEAPON_MOD.CompiledModifiers(listOf(), BLANK_WEAPON_MOD)

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