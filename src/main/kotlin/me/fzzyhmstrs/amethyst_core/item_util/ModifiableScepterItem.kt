package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * An abstract scepter integrated into the [Modifier][AbstractModifier] System.
 *
 * Does not provide any default functionality beyond gathering and initializing Modifiers for whatever use they are needed.
 */

abstract class ModifiableScepterItem<T: AbstractModifier<T>>(material: ScepterToolMaterial, settings: Settings): AbstractScepterItem(material, settings), Modifiable{

    val defaultModifiers: MutableList<Identifier> = mutableListOf()

    fun withModifiers(defaultMods: List<T> = listOf()): ModifiableScepterItem<T>{
        defaultMods.forEach {
            defaultModifiers.add(it.modifierId)
        }
        return this
    }

    override fun defaultModifiers(): MutableList<Identifier> {
        return defaultModifiers
    }

    /*override fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound) {
        super.writeDefaultNbt(stack, scepterNbt)
        addDefaultModifiers(stack, scepterNbt)
    }

    override fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound) {
        super.initializeScepter(stack, scepterNbt)
        ModifierHelper.initializeModifiers(stack, scepterNbt)
    }

    override fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean {
        return super.needsInitialization(stack, scepterNbt) || modifiersNeedInit(stack, scepterNbt)
    }

    private fun modifiersNeedInit(stack: ItemStack,scepterNbt: NbtCompound): Boolean{
        return (defaultModifiers.isNotEmpty() && !scepterNbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey))
    }

    private fun addDefaultModifiers(stack: ItemStack, scepterNbt: NbtCompound){
        if (!scepterNbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey)) {
            defaultModifiers.forEach {
                ModifierHelper.addModifier(it, stack)
            }
            scepterNbt.putBoolean(NbtKeys.MOD_INIT.str() + stack.translationKey,true)
        }
    }*/
}
