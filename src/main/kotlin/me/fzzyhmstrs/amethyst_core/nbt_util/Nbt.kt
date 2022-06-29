package me.fzzyhmstrs.amethyst_core.nbt_util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import java.util.function.Predicate

object Nbt {

    fun writeBoolNbt(key: String, state: Boolean, nbt: NbtCompound) {
        nbt.putBoolean(key, state)
    }
    fun readBoolNbt(key: String, nbt: NbtCompound): Boolean {
        return nbt.getBoolean(key)
    }
    fun writeIntNbt(key: String, input: Int, nbt: NbtCompound){
        nbt.putInt(key,input)
    }
    fun readIntNbt(key: String, nbt: NbtCompound): Int {
        return nbt.getInt(key)
    }
    fun writeLongNbt(key: String, input: Long, nbt: NbtCompound){
        nbt.putLong(key,input)
    }
    fun readLongNbt(key: String, nbt: NbtCompound): Long {
        return nbt.getLong(key)
    }
    fun writeStringNbt(key: String, input: String, nbt: NbtCompound){
        nbt.putString(key,input)
    }
    fun readStringNbt(key: String, nbt: NbtCompound): String {
        return nbt.getString(key)
    }
    fun writeBlockPos(key: String, pos: BlockPos, nbt: NbtCompound){
        nbt.putLong(key,pos.asLong())
    }
    fun readBlockPos(key: String, nbt: NbtCompound): BlockPos{
        return if (nbt.contains(key)){
            BlockPos.fromLong(nbt.getLong(key))
        } else {
            BlockPos.ORIGIN
        }
    }
    fun readNbtList(nbt: NbtCompound, key: String): NbtList {
        return if (nbt.contains(key)){
            nbt.getList(key,10)
        } else {
            NbtList()
        }
    }
    fun addNbtToList(newNbt: NbtCompound, listKey: String, baseNbt: NbtCompound){
        val nbtList = readNbtList(baseNbt, listKey)
        nbtList.add(newNbt)
        baseNbt.put(listKey,nbtList)
    }
    fun removeNbtFromList(listKey: String, baseNbt: NbtCompound, removalTest: Predicate<NbtCompound>){
        val nbtList = readNbtList(baseNbt, listKey)
        val nbtList2 = NbtList()
        for (el in nbtList){
            val nbtEl = el as NbtCompound
            if (removalTest.test(nbtEl)){
                continue
            }
            nbtList2.add(el)
        }
        baseNbt.put(listKey, nbtList2)
    }

    /**
     * method for transferring nbt between two item stacks.
     *
     * does NOT transfer enchantments. Minecraft has methods for that.
     *
     * useful for maintaining custom nbt between stacks. For example, when crafting an item into a new tier of that item, nbt can be maintained with this funciton.
     */
    fun transferNbt(stack1: ItemStack, stack2: ItemStack){
        val nbt1 = stack1.nbt ?: return
        val nbt2 = stack2.orCreateNbt
        for(nbtKey in nbt1.keys){
            if(nbtKey == ItemStack.ENCHANTMENTS_KEY){
                continue
            }
            nbt2.put(nbtKey,nbt1[nbtKey])
        }
    }

    fun getOrCreateSubCompound(nbtCompound: NbtCompound, key: String): NbtCompound {
        val subCompound = nbtCompound.get(key) ?: NbtCompound()
        if (subCompound == NbtCompound()){
            nbtCompound.put(key,subCompound)
        }
        return subCompound as NbtCompound
    }
}
