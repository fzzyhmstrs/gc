package me.fzzyhmstrs.amethyst_core.modifier_util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

interface ModifierInitializer {
    fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, list: List<Identifier>)
}