package me.fzzyhmstrs.gear_core.trinkets

import com.google.common.base.Predicate
import com.google.common.collect.Multimap
import dev.emi.trinkets.api.Trinket
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import java.util.*

object TrinketsUtil {

    private val predicates: MutableList<Predicate<NbtCompound>> = mutableListOf()

    fun isTrinket(stack: ItemStack): Boolean{
        return stack.item is Trinket
    }

    fun registerTrinketPredicate(predicate: Predicate<NbtCompound>){
        predicates.add(predicate)
    }

    fun addTrinketNbt(stack: ItemStack, nbt: NbtCompound, map: Multimap<EntityAttribute, EntityAttributeModifier>){

        if (stack.item is Trinket) {
            val existingAttributeList = nbt.getList("TrinketAttributeModifiers",10)
            val nbtList = NbtList()

            for (entry in map.entries()) {
                val attribute = entry.key
                val modifier = entry.value
                val compound = modifier.toNbt()
                val name = Registries.ATTRIBUTE.getId(attribute).toString()
                compound.putBoolean("GearCoreModifier",true)
                compound.putString("AttributeName", name)
                nbtList.add(compound)
            }

            for (attribute in existingAttributeList){
                if (attribute !is NbtCompound) continue
                if (attribute.contains("GearCoreModifier")) continue
                var cont = false
                for (predicate in predicates){
                    if (predicate.test(attribute)){
                        cont = true
                        break
                    }
                }
                if (cont) continue
                nbtList.add(attribute)
            }

            nbt.put("TrinketAttributeModifiers",nbtList)
        }
    }

    fun removeTrinketNbt(stack: ItemStack){
        val nbt = stack.nbt ?: return
        if (stack.item is Trinket) {
            val existingAttributeList = nbt.getList("TrinketAttributeModifiers",10)
            val nbtList = NbtList()

            for (attribute in existingAttributeList){
                if (attribute !is NbtCompound) continue
                if (attribute.contains("GearCoreModifier")) continue
                nbtList.add(attribute)
            }

            nbt.put("TrinketAttributeModifiers",nbtList)
        }
    }
}
