package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

object ModifierHelper: AbstractModifierHelper<AugmentModifier>() {

    override val fallbackData: AbstractModifier<AugmentModifier>.CompiledModifiers = ModifierDefaults.BLANK_COMPILED_DATA

    fun addModifierForREI(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierToNbt(modifier, nbt)
    }

    override fun initializeModifiers(stack: ItemStack, nbt: NbtCompound) {
        super.initializeModifiers(stack, nbt)
        initializeForAttunedEnchant(stack, nbt)
    }

    @Deprecated("Removing after modifiers are released for long enough. Target end of 2022.")
    private fun initializeForAttunedEnchant(stack: ItemStack, nbt: NbtCompound){
        if (stack.hasEnchantments()){
            val enchants = stack.enchantments
            var attunedLevel = 0
            var nbtEl: NbtCompound
            for (el in enchants) {
                nbtEl = el as NbtCompound
                if (EnchantmentHelper.getIdFromNbt(nbtEl) == Identifier("amethyst_imbuement","attuned")){
                    attunedLevel = EnchantmentHelper.getLevelFromNbt(nbtEl)
                    break
                }
            }
            if (attunedLevel > 0) {
                for (i in 1..attunedLevel) {
                    addModifier(ModifierRegistry.LESSER_ATTUNED.modifierId, stack, nbt)
                }
                val newEnchants = EnchantmentHelper.fromNbt(enchants)
                EnchantmentHelper.set(newEnchants,stack)
            }
        }
    }

    override fun gatherActiveModifiers(stack: ItemStack){
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) return
            val activeEnchant = Identifier(Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt))
            setModifiersById(
                id,
                gatherActiveAbstractModifiers(stack, activeEnchant, ModifierDefaults.BLANK_AUG_MOD.compiler())
            )
        }
    }
}