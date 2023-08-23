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

class GearSet private constructor(
    val id: Identifier,
    private val translationKey: String,
    private val nameDecorationTranslationKey: String,
    private val bonusDecorationTranslationKey: String,
    private val activeFormatting: Array<Formatting>,
    private val inactiveFormatting: Array<Formatting>,
    private val items: SetIngredient,
    private val attributeBonuses: MutableMap<Int, ArrayListMultimap<EntityAttribute,EntityAttributeModifier>>,
    private val modifierBonuses: MutableMap<Int,AbstractModifier.CompiledModifiers<EquipmentModifier>>) {

    private val levels: IntArray
    
    init{
        //adds all the modifier-based attribute bonuses into the actual attribute map.
        //Modifiers in the set won't actually be applying their own modifiers like normally in the EquipmentModifierHelper, because they aren't "active"
        for (entry in modifierBonuses){
            if (!entry.value.compiledData.attributeModifiers().isEmpty) {
                val multiMap = attributeBonuses.computeIfAbsent(entry.key) { ArrayListMultimap.create() }
                val modMultiMap = EquipmentModifierHelper.prepareContainerMap(null, entry.value.compiledData.attributeModifiers())
                multiMap.putAll(modMultiMap)
            }

        }
        val ints: MutableSet<Int> = mutableSetOf()
        ints.addAll(attributeBonuses.keys)
        ints.addAll(modifierBonuses.keys)
        levels = ints.toIntArray()
        levels.sort()
    }
    
    fun test(item: Item): Boolean{
        return items.test(ItemStack(item))
    }

    fun addAttributesToEntity(entity: LivingEntity, level: Int){
        if (level <= 0)
            throw IllegalStateException("GearSet level can't be below 1.")
        for (i in 1..level){
            val map = attributeBonuses[i] ?:continue
            entity.attributes.addTemporaryModifiers(map)
        }
    }
    fun removeAttributesFromEntity(entity: LivingEntity){
        for (map in attributeBonuses.values){
            entity.attributes.removeModifiers(map)
        }
    }

    fun appendTooltip(level: Int, stack: ItemStack, tooltipContext: TooltipContext, tooltip: MutableList<Text>){
        tooltip.add(AcText.empty())
        tooltip.add(AcText.translatable(nameDecorationTranslationKey,AcText.translatable(translationKey).string).formatted(*activeFormatting))
        for (i in levels){
            attributeBonuses[i]?.forEach{ attr, mod ->
                val d = mod.value
                if (d > 0.0){
                    val e = if (mod.operation == EntityAttributeModifier.Operation.MULTIPLY_BASE || mod.operation == EntityAttributeModifier.Operation.MULTIPLY_TOTAL) d * 100.0 else if (attr == EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) d * 10.0 else d
                    val attrText = Text.translatable("attribute.modifier.plus." + mod.operation.id, ItemStack.MODIFIER_FORMAT.format(e), Text.translatable(attr.translationKey))
                    tooltip.add(AcText.translatable(bonusDecorationTranslationKey,i,attrText)
                        .formatted(*if (level >= i){ activeFormatting } else { inactiveFormatting }))

                } else if (d < 0.0){
                    val e = if (mod.operation == EntityAttributeModifier.Operation.MULTIPLY_BASE || mod.operation == EntityAttributeModifier.Operation.MULTIPLY_TOTAL) d * 100.0 else if (attr == EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) d * 10.0 else d
                    val attrText = Text.translatable("attribute.modifier.take." + mod.operation.id, ItemStack.MODIFIER_FORMAT.format(e * -1.0), Text.translatable(attr.translationKey))
                    tooltip.add(AcText.translatable(bonusDecorationTranslationKey,i,attrText)
                        .formatted(*if (level >= i){ activeFormatting } else { inactiveFormatting }))
                }
            }
            modifierBonuses[i]?.modifiers?.forEach{
                val modNameDesc =  AcText.translatable(EquipmentModifierHelper.getTranslationKeyFromIdentifier(it.modifierId))
                val modDescDesc = AcText.translatable(EquipmentModifierHelper.getDescTranslationKeyFromIdentifier(it.modifierId)).formatted(Formatting.ITALIC)
                val modName = AcText.translatable("gear_core.modifier.colon",modNameDesc, modDescDesc)
                tooltip.add(
                    AcText.translatable(bonusDecorationTranslationKey,i,modName)
                        .formatted(*if (level >= i){ activeFormatting } else { inactiveFormatting }))
            }
        }
    }

    fun processPostHit(level: Int, target: LivingEntity, attacker: PlayerEntity){
        for (i in 1..level){
            modifierBonuses[i]?.compiledData?.postHit(ItemStack.EMPTY,attacker, target)
        }
    }

    fun processPostMine(level: Int, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity){
        for (i in 1..level){
            modifierBonuses[i]?.compiledData?.postMine(ItemStack.EMPTY, world, state, pos, miner)
        }
    }

    fun processOnUse(level: Int, hand: Hand, user: PlayerEntity){
        for (i in 1..level){
            modifierBonuses[i]?.compiledData?.onUse(user.getStackInHand(hand), user, null)
        }
    }

    fun processOnAttack(level: Int,amount: Float, source: DamageSource, entity: LivingEntity, attacker: LivingEntity?): Float{
        var newAmount = amount
        for (i in 1..level){
            newAmount = modifierBonuses[i]?.compiledData?.onAttack(ItemStack.EMPTY,entity,attacker, source, newAmount) ?: newAmount
        }
        return newAmount
    }

    fun processOnDamaged(level: Int,amount: Float, source: DamageSource, entity: LivingEntity, attacker: LivingEntity?): Float{
        var newAmount = amount
        for (i in 1..level){
            newAmount = modifierBonuses[i]?.compiledData?.onDamaged(ItemStack.EMPTY,entity,attacker, source, newAmount) ?: newAmount
        }
        return newAmount
    }

    fun processOnKilledOther(level: Int,playerEntity: PlayerEntity, victim: LivingEntity?){
        for (i in 1..level){
            modifierBonuses[i]?.compiledData?.killedOther(ItemStack.EMPTY,playerEntity,victim)
        }
    }

    fun processTick(level: Int,entity: LivingEntity){
        for (i in 1..level){
            modifierBonuses[i]?.compiledData?.tick(ItemStack.EMPTY, entity, null)
        }
    }

    fun getStacks(): Array<ItemStack>{
        return items.matchingStacks
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
                listOf(Formatting.GOLD, Formatting.BOLD)
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
                listOf(Formatting.DARK_GRAY)
            }
            val items = try {
                SetIngredient.fromJson(json.get("items"))
            } catch (e: JsonSyntaxException){
                throw IllegalStateException("Gear Set [$id] has an 'items' member that isn't a properly formatted Ingredient: [${e.localizedMessage}].")
            }
            if (!json.has("bonuses") || !json.get("bonuses").isJsonObject)
                throw IllegalStateException("Gear Set [$id] needs a 'bonuses' member that is a JsonObject")
            val bonusesJsonObject = json.getAsJsonObject("bonuses")
            val attributeBonuses: MutableMap<Int, ArrayListMultimap<EntityAttribute,EntityAttributeModifier>> = mutableMapOf()
            val modifierBonuses: MutableMap<Int, AbstractModifier<EquipmentModifier>.Compiler> = mutableMapOf()
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
                    modifierBonuses.computeIfAbsent(key) { EquipmentModifierHelper.BLANK_EQUIPMENT_MOD.compiler() }.add(modifier)
                } else if (bonusJson.isJsonObject){
                    val attributeJson = bonusJson.asJsonObject
                    val attribute = try {
                        val attributeIdString = attributeJson.get("attribute").asString
                        val attributeId = Identifier.tryParse(attributeIdString)
                            ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an invalid identifier value [$attributeIdString].")
                        Registries.ATTRIBUTE.get(attributeId)
                            ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an attribute value [$attributeIdString] that can't be found in the attribute registry.")
                    } catch (e: Exception){
                        throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'attribute' key [$attributeJson]. Missing or needs to be a valid identifier string.")
                    }
                    val uuid = UUID.randomUUID()
                    val name = translationKey + Registries.ATTRIBUTE.getId(attribute)
                    val value = try {
                        attributeJson.get("amount").asDouble
                    } catch (e: Exception){
                        throw IllegalStateException("Gear Set [$id] has an attribute bonus with an invalid 'amount' key. Missing or needs to be a valid number.")
                    }
                    val operation = try {
                        val operationString = attributeJson.get("operation").asString
                        EntityAttributeModifier.Operation.valueOf(operationString)
                    } catch (e: Exception){
                        throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'operation' key. Missing or needs to be a valid number.")
                    }
                    val entityAttributeModifier = EntityAttributeModifier(uuid,name,value,operation)
                    attributeBonuses.computeIfAbsent(key){ArrayListMultimap.create()}.put(attribute,entityAttributeModifier)
                }else if (bonusJson.isJsonArray){
                    val bonusesJsonArray = bonusJson.asJsonArray
                    for (b in bonusesJsonArray){
                        if (b.isJsonPrimitive){
                            val modifierIdString = b.asString
                            val modifierId = Identifier.tryParse(modifierIdString)
                                ?: throw IllegalStateException("Gear Set [$id] has a modifier bonus with an invalid identifier value [$modifierIdString].")
                            val modifier = ModifierRegistry.getByType<EquipmentModifier>(modifierId)
                                ?: throw IllegalStateException("Gear Set [$id] has a modifier bonus with a modifier value [$modifierIdString] that can't be found in the modifier registry.")
                            modifierBonuses.computeIfAbsent(key) { EquipmentModifierHelper.BLANK_EQUIPMENT_MOD.compiler() }.add(modifier)
                        } else if (b.isJsonObject){
                            val attributeJson = b.asJsonObject
                            val attribute = try {
                                val attributeIdString = attributeJson.get("attribute").asString
                                val attributeId = Identifier.tryParse(attributeIdString)
                                    ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an invalid identifier value [$attributeIdString].")
                                Registries.ATTRIBUTE.get(attributeId)
                                    ?: throw IllegalStateException("Gear Set [$id] has an attribute bonus with an attribute value [$attributeIdString] that can't be found in the attribute registry.")
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'attribute' key [$attributeJson]. Missing or needs to be a valid identifier string.")
                            }
                            val uuid = UUID.randomUUID()
                            val name = translationKey + Registries.ATTRIBUTE.getId(attribute)
                            val value = try {
                               attributeJson.get("amount").asDouble
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with an invalid 'amount' key. Missing or needs to be a valid number.")
                            }
                            val operation = try {
                                val operationString = attributeJson.get("operation").asString
                                EntityAttributeModifier.Operation.valueOf(operationString)
                            } catch (e: Exception){
                                throw IllegalStateException("Gear Set [$id] has an attribute bonus with aan invalid 'operation' key. Missing or needs to be a valid number.")
                            }
                            val entityAttributeModifier = EntityAttributeModifier(uuid,name,value,operation)
                            attributeBonuses.computeIfAbsent(key){ArrayListMultimap.create()}.put(attribute,entityAttributeModifier)
                        }
                    }
                } else {
                    throw IllegalStateException("Gear Set [$id] has an invalid 'bonuses' member..")
                }
            }
            return GearSet(id,translationKey,nameDecorationKey,bonusDecorationKey,
                activeFormattingList.toTypedArray(),inactiveFormattingList.toTypedArray(),
                items,attributeBonuses, modifierBonuses.mapValues { entry -> entry.value.compile() }.toMutableMap())
        }
    }

}
