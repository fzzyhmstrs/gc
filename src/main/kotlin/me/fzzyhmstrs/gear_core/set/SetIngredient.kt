package me.fzzyhmstrs.gear_core.set

import com.google.common.collect.ArrayListMultimap
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class SetIngredient private constructor(private val items: List<Identifier>, private val tags: List<Identifier>){

    private val itemsCached: List<Item> by lazy{
        items.stream().map{Registries.ITEM.get(it).takeIf{it != Item.AIR}}.filterNotNull()
    }
    private val tagsCached List<TagKey<Item>> by lazy{
        tags.streams().map{TagKey.of(RegistryKeys.ITEM,it)}.toList()
    }
    
    fun test(stack: ItemStack){
        val item = stack.item
        if (itemsCached.contains(item)) return true
        for (tag in tagsCached){
            if (stack.isIn(tag) return true
        }
        return false
    }

    companion object{

        fun fromJson(json: JsonElement): SetIngredient{                
            if (json.isJsonObject){
                val jsonObject = json.asJsonObject
                if (jsonObject.has("item")){
                    val itemString = json.getAsJsonPrimitive("item").asString
                    val itemId = Identifier.tryParse(itemString) ?: IllegalStateException("Invalid Identifier string in the 'item' member of a SetIngredient object.")
                    return SetIngredient(listOf(itemId), listOf())
                }else if (json.has("tag")){
                    val tagString = json.getAsJsonPrimitive("tag").asString
                    val tagId = Identifier.tryParse(tagString) ?: IllegalStateException("Invalid Identifier string in the 'tag' member of a SetIngredient object.")
                    return SetIngredient(listOf(), listOf(tagId))
                } else {
                    throw IllegalStateException("Expecting 'item' or 'tag' member in the SetIngredient object")
                }          
            } else if (json.isJsonArray){
                val jsonArray = json.asJsonArray
                val items: MutableList<Identifier> = mutableListOf()
                val tags: MutableList<Identifier> = mutableListOf()
                for (jsonEl in jsonArray){
                    if (!jsonEl.isJsonObject) throw IllegalStateException("Improperly formatted SetIngredient array member. Needs to be a JsonObject: $jsonEl")
                    val jsonObject = jsonEl.asJsonObject
                    if (jsonObject.has("item")){
                        val itemString = json.getAsJsonPrimitive("item").asString
                        val itemId = Identifier.tryParse(itemString) ?: IllegalStateException("Invalid Identifier string in the 'item' member of a SetIngredient object.")
                        items.add(itemId)
                    } else if (json.has("tag")){
                        val tagString = json.getAsJsonPrimitive("tag").asString
                        val tagId = Identifier.tryParse(tagString) ?: IllegalStateException("Invalid Identifier string in the 'tag' member of a SetIngredient object.")
                        tags.add(tagId)
                    } else {
                        throw IllegalStateException("Expecting 'item' or 'tag' member in the SetIngredient object")
                    }
                }
                return SetIngredient(items,tags)
            }
            throw IllegalStateException("Improperly formatted SetIngredient. Needs to be a JsonObject or JsonArray: $json")
        }
    
    }
  
}
