package me.fzzyhmstrs.gear_core.set

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.gear_core.modifier_util.EntityAttributeModifierContainer
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.lang.IllegalStateException
import javax.swing.text.html.HTML.Tag.P

class GearSet private constructor(
    private val id: Identifier,
    private val translationKey: String,
    private val nameDecorationTranslationKey: String,
    private val bonusDecorationTranslationKey: String,
    private val activeFormatting: Array<Formatting>,
    private val inactiveFormatting: Array<Formatting>,
    private val items: Ingredient,
    private val attributeBonuses: Map<Int, ArrayListMultimap<EntityAttribute,EntityAttributeModifier>>,
    private val modifierBonuses: Map<Int,List<EquipmentModifier>>) {

    init{
        //adds all the modifier-based attribute bonuses into the actual attribute map.
        //Modifiers in the set won't actually be applying their own modifiers like normally in the EquipmentModifierHelper, because they aren't "active"
        for (entry in modifierBonuses){
            if (mod.attributeModifiers().isNotEmpty()) {
                val multiMap = attributeBonuses.computeIfAbsent(entry.key){ArrayListMultimap.create()}
                for (mod in entry.value){
                    val modMultiMap = EquipmentModifierHelper.prepareContainerMap(null,mod.attributeModifiers())
                    multiMap.putAll(modMultiMap)
                }
            }
        }
    }
    
    fun test(item: Item): Boolean{
        return items.test(ItemStack(item))
    }

    fun addAttributesToEntity(entity: LivingEntity, level: Int){
        if (level <= 0)
            throw IllegalStateException("GearSet level can't be below 1.")
        for (i in 1..level){
            val map = attributeBonuses.get(i)?:continue
            entity.getAttributes().addTemporaryModifiers(map)
        }
    }
    fun removeAttributesFromEntity(entity: LivingEntity){
        for (map in attributeBonuses.values()){
            entity.getAttributes().removeModifiers(map)
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is GearSet) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object{
        fun fromJson(id: Identifier,json: JsonObject): GearSet{
            val translationKey = try {
                json.getAsJsonPrimitive("name").asString
            } catch (e: Exception){
                throw IllegalStateException("Gear Set [$id] needs a 'name' member that is a string")
            }
            val nameDecorationKey = if (json.has("name_decoration")) {
                try {
                    json.getAsJsonPrimitive("name_decoration").asString
                } catch (e: Exception){
                    throw IllegalStateException("Gear Set [$id] has an optional 'name_decoration' member that isn't a string")
                }
            } else {
                "gear_core.set.name_brackets"
            }
            val bonusDecorationKey = if (json.has("bonus_decoration")) {
                try {
                    json.getAsJsonPrimitive("bonus_decoration").asString
                } catch (e: Exception){
                    throw IllegalStateException("Gear Set [$id] has an optional 'bonus_decoration' member that isn't a string")
                }
            } else {
                "gear_core.set.bonus_brackets"
            }
            val activeFormattingList: List<Formatting> = if (json.has("active_formatting")) {
                try {
                    json.getAsJsonArray("active_formatting")
                        .map { jsonElement -> jsonElement.asString }
                        .map { str -> Formatting.byName(str) }
                        .filterNotNull()
                } catch (e: Exception) {
                    throw IllegalStateException("Gear Set [$id] has an 'active_formatting' member that isn't a properly formatted array.")
                }
            } else {
                listOf(Formatting.GOLD)
            }
            val inactiveFormattingList: List<Formatting> = if (json.has("inactive_formatting")) {
                try {
                    json.getAsJsonArray("inactive_formatting")
                        .map { jsonElement -> jsonElement.asString }
                        .map { str -> Formatting.byName(str) }
                        .filterNotNull()
                } catch (e: Exception) {
                    throw IllegalStateException("Gear Set [$id] has an 'inactive_formatting' member that isn't a properly formatted array.")
                }
            } else {
                listOf(Formatting.GOLD)
            }
            val items = try {
                Ingredient.fromJson(json.get("items"))
            } catch (e: JsonSyntaxException){
                throw IllegalStateException("Gear Set [$id] has an 'items' member that isn't a properly formatted Ingredient: [${e.localizedMessage}].")
            }
            if (!json.has("bonuses") || !json.get("bonuses").isJsonObject)
                throw IllegalStateException("Gear Set [$id] needs a 'bonuses' member that is a JsonObject")
            val bonusesJsonObject = json.getAsJsonObject("bonuses")
            val attributeBonuses: MutableMap<Int, ArrayListMultimap<EntityAttribute,EntityAttributeModifier>> = mutableMapOf()
            val modifierBonuses: MutableMap<Int,MutableList<EquipmentModifier>> = mutableMapOf()
            for (bonus in bonusesJsonObject.entrySet()){
                val key = try {
                    val chk = bonus.key.toInt()
                    if (chk <= 0)
                        throw java.lang.NumberFormatException()
                    chk
                } catch (e: NumberFormatException){
                    throw java.lang.NumberFormatException("Gear Set [$id] has a bonus with an invalid key [${bonus.key}]. Needs to be a positive integer greater than 0")
                }
                val bonusJson = bonus.value
                if (bonusJson.isJsonPrimitive){
                    val modifierJson = bonusJson.asString
                    val modifierId = Identifier.tryParse(modifierJson)
                        ?: throw IllegalStateException("Gear Set [$id] has a bonus with an invalid identifier value [$modifierJson]. Needs to be a valid identifier string")
                    val modifier = ModifierRegistry.getByType<EquipmentModifier>(modifierId)
                        ?: throw IllegalStateException("Gear Set [$id] has a bonus with a modifier value [$modifierJson] that can't be found in the modifier registry.")
                    modifierBonuses.computeIfAbsent(key) { mutableListOf() }.add(modifier)
                } else if (bonusJson.isJsonArray){
                    val bonusesJsonArray = bonusJson.asJsonArray
                    for (b in bonusesJsonArray){
                        if (b.isJsonPrimitive){
                            val modifierIdString = bonusJson.asString
                            val modifierId = Identifier.tryParse(modifierIdString)
                                ?: throw IllegalStateException("Gear Set [$id] has a modifier bonus with an invalid identifier value [$modifierIdString].")
                            val modifier = ModifierRegistry.getByType<EquipmentModifier>(modifierId)
                                ?: throw IllegalStateException("Gear Set [$id] has a modifier bonus with a modifier value [$modifierIdString] that can't be found in the modifier registry.")
                            modifierBonuses.computeIfAbsent(key) { mutableListOf() }.add(modifier)
                        } else if (b.isJsonObject){
                            val attributeJson = b.asJsonObject
                            val attribute = try {
                                val attributeIdString = attributeJson.get("attribute").asString
                                val attributeId = Identifier.tryParse(modifierJson)
                                    ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an invalid identifier value [$attributeIdString].")
                                Registries.ENTITY_ATTRIBUTE.get(attributeId)
                                    ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an attribute value [$attributeIdString] that can't be found in the attribute registry.")
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'attribute' key [$attributeJson]. Missing or needs to be a valid identifier string.")
                            }
                            val uuid = UUID.randomUuid()
                            val name = translationKey + Registries.ENTITY_ATTRIBUTE.getId(attribute)
                            val value = try {
                               attributeJson.get("value").asDouble
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'value' key [$modifierIdString]. Missing or needs to be a valid number.")
                            }
                            val operation = try {
                                val operationString = attributeJson.get("operation").asString
                                EntityAttributeModifier.Operation.valueOf(operationString)
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'value' key [$modifierIdString]. Missing or needs to be a valid number.")
                            }
                            val entityAttributeModifier = EntityAttributeModifier(uuid,name,value,operation)
                            attributeBonuses.computeIfAbsent(key){ArrayListMultimap.create()}.put(attribute,entityAttributeModifier)
                        }
                    }
                } else {
                    throw IllegalStateException("Gear Set [$id] has an invalid 'bonuses' member. Needs to be a string or an array.")
                }
            }
            return GearSet(id,translationKey,nameDecorationKey,bonusDecorationKey,activeFormattingList.toTypedArray(),inactiveFormattingList.toTypedArray(),items,attributeBonuses, modifierBonuses)
        }
    }

}
