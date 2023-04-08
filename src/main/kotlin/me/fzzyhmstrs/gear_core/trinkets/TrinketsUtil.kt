package me.fzzyhmstrs.gear_core.trinkets

import com.google.common.collect.Multimap
import dev.emi.trinkets.api.Trinket
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.registry.Registry
import net.minecraft.nbt.NbtList

object TrinketsUtil {
    
    fun isTrinket(stack: ItemStack): Boolean{
        return stack.item is Trinket
    }

    fun addTrinketNbt(stack: ItemStack,nbt: NbtCompound, map: Multimap<EntityAttribute, EntityAttributeModifier>){

        if (stack.item is Trinket) {
            val existingAttributeList = nbt.getList("TrinketAttributeModifiers",10)
            val nbtList = NbtList()

            for (entry in map.entries()) {
                val attribute = entry.key
                val modifier = entry.value
                val compound = modifier.toNbt()
                val name = Registry.ATTRIBUTE.getId(attribute).toString()
                compound.putBoolean("GearCoreModifier",true)
                compound.putString("AttributeName", name)
                nbtList.add(compound)
            }

            for (attribute in existingAttributeList){
                if (attribute !is NbtCompound) continue
                if (attribute.contains("GearCoreModifier")) continue
                nbtList.add(attribute)
            }

            nbt.put("TrinketAttributeModifiers",nbtList)
        }
    }
}
