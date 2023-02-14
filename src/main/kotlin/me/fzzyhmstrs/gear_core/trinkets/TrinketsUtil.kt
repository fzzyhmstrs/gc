package me.fzzyhmstrs.gear_core.trinkets

import com.google.common.collect.Multimap
import dev.emi.trinkets.api.Trinket
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries

object TrinketsUtil {

    fun addTrinketNbt(stack: ItemStack,nbt: NbtCompound, map: Multimap<EntityAttribute, EntityAttributeModifier>){
        if (stack.item is Trinket) {
            nbt.remove("TrinketAttributeModifiers")
            for (entry in map.entries()) {
                val attribute = entry.key
                val modifier = entry.value
                val compound = modifier.toNbt()
                compound.putString("AttributeName", Registries.ATTRIBUTE.getId(attribute).toString())
                Nbt.addNbtToList(compound, "TrinketAttributeModifiers", nbt)
            }
        }
    }

}