package me.fzzyhmstrs.amethyst_core.nbt_util

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.nbt.NbtCompound

object NbtScepterHelper {

    fun checkLastUsed(nbtCompound: NbtCompound, activeEnchantId: String, currentTime: Long, time: Long): Long{
        val key = activeEnchantId + NbtKeys.LAST_USED.str()
        val lastUsedList = nbtCompound.get(NbtKeys.LAST_USED_LIST.str())
        return if (lastUsedList == null){
            createLastUsedList(nbtCompound)
            time
        } else {
            if (!(lastUsedList as NbtCompound).contains(key)) {
                Nbt.writeLongNbt(key, time, lastUsedList)
                time
            } else {
                val timeToReturn = Nbt.readLongNbt(key, nbtCompound)
                Nbt.writeLongNbt(key, currentTime, lastUsedList)
                timeToReturn
            }
        }
    }

    fun createLastUsedList(nbtCompound: NbtCompound){
        val lastUsedList = NbtCompound()
        nbtCompound.put(NbtKeys.LAST_USED_LIST.str(),lastUsedList)
    }

    fun validateLastUsedNbt(nbtCompound: NbtCompound){
        if (!nbtCompound.contains(NbtKeys.LAST_USED_LIST.str())) return
        val lastUsedList = nbtCompound.get(NbtKeys.LAST_USED_LIST.str())
        val enchants = EnchantmentHelper.get()
    }

}