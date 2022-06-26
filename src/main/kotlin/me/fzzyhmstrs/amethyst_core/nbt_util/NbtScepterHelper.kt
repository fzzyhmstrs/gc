package me.fzzyhmstrs.amethyst_core.nbt_util

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

object NbtScepterHelper {

    fun checkLastUsed(lastUsedList: NbtCompound, activeEnchantId: String, time: Long): Long{
        val key = activeEnchantId + NbtKeys.LAST_USED.str()
        return if (!lastUsedList.contains(key)) {
            Nbt.writeLongNbt(key, time, lastUsedList)
            time
        } else {
            Nbt.readLongNbt(key, lastUsedList)
        }
    }
    fun updateLastUsed(lastUsedList: NbtCompound, activeEnchantId: String, currentTime: Long){
        val key = activeEnchantId + NbtKeys.LAST_USED.str()
        Nbt.writeLongNbt(key, currentTime, lastUsedList)

    }

    fun getOrCreateLastUsedList(nbtCompound: NbtCompound): NbtCompound{
        val lastUsedList = nbtCompound.get(NbtKeys.LAST_USED_LIST.str())
        return if (lastUsedList == null){
            createLastUsedList(nbtCompound)
        } else {
            lastUsedList as NbtCompound
        }
    }

    private fun createLastUsedList(nbtCompound: NbtCompound): NbtCompound{
        val lastUsedList = NbtCompound()
        nbtCompound.put(NbtKeys.LAST_USED_LIST.str(),lastUsedList)
        return lastUsedList
    }

}