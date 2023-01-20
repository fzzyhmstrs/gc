package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.modifier_util.*
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.function.SetNbtLootFunction
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier

/**
 * registers Modifiers. Comes with a short list of default modifiers and debugging modifiers for use with Augment-style Scepters
 *
 * This registry accepts any modifier based on the [AbstractModifier] system, and provides methods for interacting with specific Modifier types.
 */

@Suppress("MemberVisibilityCanBePrivate")
object ModifierRegistry {
    private val registry: MutableMap<Identifier, AbstractModifier<*>> = mutableMapOf()

    /**
     * example harmful [AugmentConsumer] that applies wither to targets specified to receive Harmful effects in the [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment] implementation.
     */
    private val DEBUG_NECROTIC_CONSUMER = AugmentConsumer({ list: List<LivingEntity> -> necroticConsumer(list)}, AugmentConsumer.Type.HARMFUL)
    private fun necroticConsumer(list: List<LivingEntity>){
        list.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.WITHER,80)
            )
        }
    }

    /**
     * example beneficial [AugmentConsumer] that applies regeneration to targets specified to receive beneficial effects. Most commonly, this will be the player than cast the [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment], but may also be other targets of, for example, a mass healing spell.
     */
    private val DEBUG_HEALING_CONSUMER = AugmentConsumer({ list: List<LivingEntity> -> healingConsumer(list)}, AugmentConsumer.Type.BENEFICIAL)
    private fun healingConsumer(list: List<LivingEntity>){
        list.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.REGENERATION,40)
            )
        }
    }

    /**
     * built-in modifiers. Attuned and Thrifty are provided with Imbuing recipes for use with _Amethyst Imbuement_ by default.
     *
     * Amethyst Imbuement namespace kept for Reach and Enduring lineages to avoid breaking changes in-game
     */
    val GREATER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"greater_attuned"), cooldownModifier = -22.5)
    val ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"attuned"), cooldownModifier = -15.0).withDescendant(GREATER_ATTUNED)
    val LESSER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"lesser_attuned"), cooldownModifier = -7.5).withDescendant(ATTUNED)
    val GREATER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"greater_thrifty"), manaCostModifier = -15.0)
    val THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"thrifty"), manaCostModifier = -10.0).withDescendant(GREATER_THRIFTY)
    val LESSER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"lesser_thrifty"), manaCostModifier = -5.0).withDescendant(THRIFTY)
    val GREATER_REACH = AugmentModifier(Identifier("amethyst_imbuement","greater_reach")).withRange(rangePercent = 24.0)
    val REACH = AugmentModifier(Identifier("amethyst_imbuement","reach")).withDescendant(GREATER_REACH).withRange(rangePercent = 16.0)
    val LESSER_REACH = AugmentModifier(Identifier("amethyst_imbuement","lesser_reach")).withDescendant(REACH).withRange(rangePercent = 8.0)
    val GREATER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","greater_enduring")).withDuration(durationPercent = 65)
    val ENDURING = AugmentModifier(Identifier("amethyst_imbuement","enduring")).withDescendant(GREATER_ENDURING).withDuration(durationPercent = 30)
    val LESSER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","lesser_enduring")).withDescendant(ENDURING).withDuration(durationPercent = 15)
    val MODIFIER_DEBUG = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug")).withDamage(2.0F,2.0F).withRange(2.75)
    val MODIFIER_DEBUG_2 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_2"), levelModifier = 1).withDuration(10, durationPercent = 15).withAmplifier(1)
    val MODIFIER_DEBUG_3 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_3")).withConsumer(DEBUG_HEALING_CONSUMER).withConsumer(DEBUG_NECROTIC_CONSUMER)

    internal fun registerAll(){
        register(GREATER_ATTUNED)
        register(ATTUNED)
        register(LESSER_ATTUNED)
        register(GREATER_THRIFTY)
        register(THRIFTY)
        register(LESSER_THRIFTY)
        register(GREATER_REACH)
        register(REACH)
        register(LESSER_REACH)
        register(GREATER_ENDURING)
        register(ENDURING)
        register(LESSER_ENDURING)
        register(MODIFIER_DEBUG)
        register(MODIFIER_DEBUG_2)
        register(MODIFIER_DEBUG_3)
    }

    /**
     * register a modifier with this.
     */
    fun register(modifier: AbstractModifier<*>){
        val id = modifier.modifierId
        if (registry.containsKey(id)){throw IllegalStateException("AbstractModifier with id $id already present in ModififerRegistry")}
        registry[id] = modifier
    }
    fun get(id: Identifier): AbstractModifier<*>?{
        return registry[id]
    }
    fun getByRawId(rawId: Int): AbstractModifier<*>?{
        return registry[getIdByRawId(rawId)]
    }
    fun getIdByRawId(rawId:Int): Identifier {
        return registry.keys.elementAtOrElse(rawId) { ModifierDefaults.BLANK_ID }
    }
    fun getRawId(id: Identifier): Int{
        return registry.keys.indexOf(id)
    }
    fun isModifier(id: Identifier): Boolean{
        return this.get(id) != null
    }

    /**
     * get method that wraps in a type check, simplifying retrieval of only the relevant modifier type.
     */
    inline fun <reified T: AbstractModifier<T>> getByType(id: Identifier): T?{
        val mod = get(id)
        return if (mod is T){
            mod
        } else {
            null
        }
    }

    /**
     * Alternative get-by-type that does reflective class checking.
     */
    fun <T: AbstractModifier<T>>getByType(id: Identifier, classType: Class<T>): T?{
        val mod = get(id)
        return if (mod?.javaClass?.isInstance(classType) == true){
            try {
                mod as T
            } catch(e: ClassCastException){
                return null
            }
        } else {
            null
        }
    }

    /**
     * [LootFunction.Builder] usable with loot pool building that will add default modifiers, a provided list of modifiers, or both.
     */
    fun modifiersLootFunctionBuilder(item: Item, modifiers: List<AbstractModifier<*>> = listOf(), helper: AbstractModifierHelper<*>): LootFunction.Builder{
        val modList = NbtList()
        if (item is Modifiable) {
            if (item.defaultModifiers().isEmpty() && modifiers.isEmpty()){
                return SetEnchantmentsLootFunction.Builder() //empty builder for placehold purposes basically
            } else {
                item.defaultModifiers().forEach {
                    val nbtEl = NbtCompound()
                    Nbt.writeStringNbt(NbtKeys.MODIFIER_ID.str(),it.toString(),nbtEl)
                    modList.add(nbtEl)
                }
                modifiers.forEach {
                    if (it.isAcceptableItem(ItemStack(item))) {
                        val nbtEl = NbtCompound()
                        Nbt.writeStringNbt(NbtKeys.MODIFIER_ID.str(), it.toString(), nbtEl)
                        modList.add(nbtEl)
                    }
                }
            }
        } else if (modifiers.isEmpty()) {
            return SetEnchantmentsLootFunction.Builder()
        } else {
            modifiers.forEach {
                val nbtEl = NbtCompound()
                Nbt.writeStringNbt(NbtKeys.MODIFIER_ID.str(),it.toString(),nbtEl)
                modList.add(nbtEl)
            }
        }
        val nbt = NbtCompound()
        nbt.put(NbtKeys.MODIFIERS.str(), modList)
        return SetNbtLootFunction.builder(nbt)
    }
}