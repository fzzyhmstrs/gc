package me.fzzyhmstrs.gear_core.trinkets

import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries

object TrinketsUtil {

    fun addTrinketNbt(nbt: NbtCompound, map: Multimap<EntityAttribute, EntityAttributeModifier>){
        nbt.remove("TrinketAttributeModifiers")
        for (entry in map.entries()) {
            val attribute = entry.key
            val modifier = entry.value
            val compound = modifier.toNbt()
            compound.putString("AttributeName", Registries.ATTRIBUTE.getId(attribute).toString())
            Nbt.addNbtToList(compound,"TrinketAttributeModifiers",nbt)
        }
    }

}