package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier

/**
 * An abstract scepter integrated into the [Modifier][AbstractModifier] System.
 *
 * Does not provide any default functionality beyond gathering and initializing Modifiers for whatever use they are needed.
 */

abstract class ModifiableScepterItem<T: AbstractModifier<T>>(material: ScepterToolMaterial, settings: Settings): AbstractScepterItem(material, settings), Modifiable<T>{

    override val defaultModifiers: MutableList<Identifier> = mutableListOf()

    fun withModifiers(defaultMods: List<T> = listOf()): ModifiableScepterItem<T>{
        defaultMods.forEach {
            defaultModifiers.add(it.modifierId)
        }
        return this
    }

    override fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound){
        super.writeDefaultNbt(stack, scepterNbt)
        if (!scepterNbt.contains(NbtKeys.MODIFIERS.str())) {
            val nbtList = NbtList()
            defaultModifiers.forEach {
                val nbtEl = NbtCompound()
                Nbt.writeStringNbt(NbtKeys.MODIFIER_ID.str(), it.toString(), nbtEl)
                nbtList.add(nbtEl)
            }
            scepterNbt.put(NbtKeys.MODIFIERS.str(), nbtList)
        }
    }

    override fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound) {
        super.initializeScepter(stack, scepterNbt)
        if (modifiersNeedInit(scepterNbt)){
            writeDefaultNbt(stack, scepterNbt)
        }
        ModifierHelper.initializeModifiers(stack, scepterNbt)
    }

    override fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean {
        return super.needsInitialization(stack, scepterNbt) || modifiersNeedInit(scepterNbt) || Nbt.getItemStackId(scepterNbt) == -1L
    }

    private fun modifiersNeedInit(scepterNbt: NbtCompound): Boolean{
        return (defaultModifiers.isNotEmpty() && !scepterNbt.contains(NbtKeys.MODIFIERS.str()))
    }



}
