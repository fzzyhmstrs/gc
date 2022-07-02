package me.fzzyhmstrs.amethyst_core.nbt_util

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.function.Predicate

/**
 * simple functions that arrange Nbt functions in a way I prefer and add some functionalities vanilla nbt doens't make as easy
 */
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

    /**
     * stores blockpos on the object as a long, and then back converts it. This method is used as opposed to an X/Y/Z three tag implementation for brevity.
     */
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

    /**
     * NbtList tools. Read a list, add a component to a list, or remove a component
     */
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
     * utility for providing an itemstack with a unique and relatively immutable identifier. This allows for an itemstack to be uniquely tracked even across crafting, enchanting, etc. etc.
     */
    fun makeItemStackId(stack: ItemStack): Long{
        val nbt = stack.orCreateNbt
        return if (!nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            val long = (AC.acRandom.nextDouble() * Long.MAX_VALUE).toLong()
            writeLongNbt(NbtKeys.ITEM_STACK_ID.str(),long,nbt)
            long
        } else {
            getItemStackId(nbt)
        }
    }
    fun getItemStackId(stack: ItemStack): Long{
        val nbt = stack.orCreateNbt
        return if (nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            readLongNbt(NbtKeys.ITEM_STACK_ID.str(), nbt)
        } else {
            -1L
        }
    }
    fun getItemStackId(nbt: NbtCompound): Long{
        return if (nbt.contains(NbtKeys.ITEM_STACK_ID.str())){
            readLongNbt(NbtKeys.ITEM_STACK_ID.str(), nbt)
        } else {
            -1L
        }
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
