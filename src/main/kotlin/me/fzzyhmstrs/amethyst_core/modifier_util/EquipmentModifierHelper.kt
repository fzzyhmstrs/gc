package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val BLANK_WEAPON_MOD = EquipmentModifier(ModifierDefaults.BLANK_ID)
    override val fallbackData: AbstractModifier<EquipmentModifier>.CompiledModifiers
        get() = BLANK_WEAPON_MOD.CompiledModifiers(listOf(), EquipmentModifier(ModifierDefaults.BLANK_ID))

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

    override fun getTranslationKeyFromIdentifier(id: Identifier): String {
        return "equipment.modifier.${id}"
    }

}