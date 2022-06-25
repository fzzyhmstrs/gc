package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlD
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlF
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlI
import me.fzzyhmstrs.amethyst_core.item_util.AcceptableItemStacks
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.amethyst_core.scepter_util.AugmentConsumer.*
import me.fzzyhmstrs.amethyst_core.scepter_util.ModifierDefaults.BLANK_EFFECT
import me.fzzyhmstrs.amethyst_core.scepter_util.ModifierDefaults.BLANK_XP_MOD
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.max

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
    //alternative version with the AugmentEffect directly included

object ModifierDefaults{
    val BLANK_ID = Identifier(AC.MOD_ID,"blank_modifier")
    val BLANK_EFFECT = AugmentEffect()
    val BLANK_XP_MOD = XpModifiers()
    val EMPTY_COMPILED = CompiledAugmentModifier(BLANK_ID)
    val BLANK_COMPILED_DATA = CompiledAugmentModifier.CompiledModifiers(listOf(), EMPTY_COMPILED)
}

open class AbstractModifier(val modifierId: Identifier){

    private var descendant: Identifier = ModifierDefaults.BLANK_ID
    private val lineage: List<Identifier> by lazy { generateLineage() }
    private var objectsToAffect: Predicate<Identifier>? = null

    private var hasDesc: Boolean = false
    private var hasObjectToAffect: Boolean = false

    fun hasDescendant(): Boolean{
        return hasDesc
    }
    fun addDescendant(modifier: AbstractModifier){
        val id = modifier.modifierId
        descendant = id
        hasDesc = true
    }
    fun getModLineage(): List<Identifier>{
        return lineage
    }
    private fun generateLineage(): List<Identifier>{
        val nextInLineage = ModifierRegistry.get(descendant)
        val lineage: MutableList<Identifier> = mutableListOf(this.modifierId)
        lineage.addAll(nextInLineage?.getModLineage() ?: listOf())
        return lineage
    }
    open fun hasObjectToAffect(): Boolean{
        return hasObjectToAffect
    }
    open fun addObjectToAffect(predicate: Predicate<Identifier>){
        objectsToAffect = predicate
        hasObjectToAffect = true
    }
    open fun checkObjectsToAffect(id: Identifier): Boolean{
        return objectsToAffect?.test(id) ?: return false
    }
    open fun getName(): Text {
        return LiteralText("$modifierId")
    }
    open fun isAcceptableItem(stack: ItemStack): Boolean{
        acceptableItemStacks().forEach {
            if (stack.isOf(it.item)){
                return true
            }
        }
        return false
    }
    open fun acceptableItemStacks(): MutableList<ItemStack>{
        return mutableListOf()
    }
}

open class AugmentModifier(
    modifierId: Identifier,
    open val levelModifier: Int = 0,
    open val cooldownModifier: Double = 0.0,
    open val manaCostModifier: Double = 0.0
    ): AbstractModifier(modifierId) {

    protected val effects: AugmentEffect = AugmentEffect()
    protected val xpModifier: XpModifiers = XpModifiers()
    private var secondaryEffect: ScepterAugment? = null
        
    private var hasSecondEffect: Boolean = false
    private var hasDesc: Boolean = false

    fun hasSpellToAffect(): Boolean{
        return hasObjectToAffect()
    }
    fun checkSpellsToAffect(id: Identifier): Boolean{
        return checkObjectsToAffect(id)
    }
    fun hasSecondaryEffect(): Boolean{
        return hasSecondEffect
    }
    fun getSecondaryEffect(): ScepterAugment?{
        return secondaryEffect
    }
    fun getEffectModifier(): AugmentEffect {
        return effects
    }
    fun getXpModifiers(): XpModifiers {
        return xpModifier
    }
    
    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentModifier {
        effects.plus(BLANK_EFFECT.withDamage(damage, damagePerLevel, damagePercent))
        return this
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentModifier {
        effects.plus(BLANK_EFFECT.withAmplifier(amplifier, amplifierPerLevel, amplifierPercent))
        return this
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentModifier {
        effects.plus(BLANK_EFFECT.withDuration(duration, durationPerLevel, durationPercent))
        return this
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentModifier {
        effects.plus(BLANK_EFFECT.withRange(range, rangePerLevel, rangePercent))
        return this
    }
    fun withSecondaryEffect(effect: AugmentEffect): AugmentModifier {
        effects.plus(effect)
        return this
    }
    fun withXpMod(type: SpellType, xpMod: Int): AugmentModifier {
        val xpMods = when(type){
                SpellType.FURY ->{
                    BLANK_XP_MOD.withFuryMod(xpMod)}
                SpellType.WIT ->{
                    BLANK_XP_MOD.withWitMod(xpMod)}
                SpellType.GRACE ->{
                    BLANK_XP_MOD.withGraceMod(xpMod)}
                else -> return this
            }
        xpModifier.plus(xpMods)
        return this
    }
    fun withSpellToAffect(predicate: Predicate<Identifier>): AugmentModifier {
        addObjectToAffect(predicate)
        return this
    }
    fun withSecondaryEffect(augment: ScepterAugment): AugmentModifier {
        secondaryEffect = augment
        hasSecondEffect = true
        return this
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: Type): AugmentModifier {
        effects.withConsumer(consumer,type)
        return this
    }
    fun withConsumer(augmentConsumer: AugmentConsumer): AugmentModifier {
        effects.withConsumer(augmentConsumer.consumer,augmentConsumer.type)
        return this
    }
    fun withDescendant(modifier: AugmentModifier): AugmentModifier {
        addDescendant(modifier)
        return this
    }

    override fun acceptableItemStacks(): MutableList<ItemStack>{
        return AcceptableItemStacks.scepterAcceptableItemStacks(1)
    }

}

class CompiledAugmentModifier(
    modifierId: Identifier = ModifierDefaults.BLANK_ID,
    override var levelModifier: Int = 0,
    override var cooldownModifier: Double = 0.0,
    override var manaCostModifier: Double = 0.0)
    : AugmentModifier(modifierId, levelModifier, cooldownModifier, manaCostModifier){

    fun plus(am: AugmentModifier) {
        levelModifier += am.levelModifier
        cooldownModifier += am.cooldownModifier
        manaCostModifier += am.manaCostModifier
        xpModifier.plus(am.getXpModifiers())
        effects.plus(am.getEffectModifier())
    }

    data class CompiledModifiers(val modifiers: List<AugmentModifier>, val compiledData: CompiledAugmentModifier)
}

data class AugmentEffect(
    private var damageData: PerLvlF = PerLvlF(),
    private var amplifierData: PerLvlI = PerLvlI(),
    private var durationData: PerLvlI = PerLvlI(),
    private var rangeData: PerLvlD = PerLvlD()
){
    private var goodConsumers: MutableList<AugmentConsumer> = mutableListOf()
    private var badConsumers: MutableList<AugmentConsumer> = mutableListOf()

    fun plus(ae: AugmentEffect){
        damageData = damageData.plus(ae.damageData)
        amplifierData = amplifierData.plus(ae.amplifierData)
        durationData = durationData.plus(ae.durationData)
        rangeData = rangeData.plus(ae.rangeData)
        goodConsumers.addAll(ae.goodConsumers)
        badConsumers.addAll(ae.badConsumers)
    }
    fun damage(level: Int = 0): Float{
        return max(0.0F, damageData.value(level))
    }
    fun amplifier(level: Int = 0): Int{
        return max(0,amplifierData.value(level))
    }
    fun duration(level: Int = 0): Int{
        return max(0,durationData.value(level))
    }
    fun range(level: Int = 0): Double{
        return max(1.0,rangeData.value(level))
    }
    fun consumers(): MutableList<AugmentConsumer>{
        val list = mutableListOf<AugmentConsumer>()
        list.addAll(goodConsumers)
        list.addAll(badConsumers)
        return list
    }
    fun accept(list: List<LivingEntity>,type: Type? = null){
        when (type){
            Type.BENEFICIAL->{
                goodConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
            Type.HARMFUL->{
                badConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
            else->{
                goodConsumers.forEach {
                    it.consumer.accept(list)
                }
                badConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
        }
    }
    fun accept(entity: LivingEntity, type: Type? = null){
        accept(listOf(entity), type)
    }

    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentEffect {
        return this.copy(damageData = PerLvlF(damage, damagePerLevel, damagePercent))
    }
    fun addDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData.plus(PerLvlF(damage, damagePerLevel, damagePercent))
    }
    fun addDamage(ae: AugmentEffect){
        damageData.plus(ae.damageData)
    }
    fun setDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData = PerLvlF(damage, damagePerLevel, damagePercent)
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect {
        return this.copy(amplifierData = PerLvlI(amplifier,amplifierPerLevel,amplifierPercent))
    }
    fun addAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData.plus(PerLvlI(amplifier,amplifierPerLevel,amplifierPercent))
    }
    fun addAmplifier(ae: AugmentEffect){
        amplifierData.plus(ae.amplifierData)
    }
    fun setAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData = PerLvlI(amplifier,amplifierPerLevel,amplifierPercent)
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect {
        return this.copy(durationData = PerLvlI(duration, durationPerLevel, durationPercent))
    }
    fun addDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData.plus(PerLvlI(duration, durationPerLevel, durationPercent))
    }
    fun addDuration(ae: AugmentEffect){
        durationData.plus(ae.durationData)
    }
    fun setDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData = PerLvlI(duration, durationPerLevel, durationPercent)
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect {
        return this.copy(rangeData = PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData.plus(PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(ae: AugmentEffect){
        rangeData.plus(ae.rangeData)
    }
    fun setRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData = PerLvlD(range, rangePerLevel, rangePercent)
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: Type): AugmentEffect {
        addConsumer(consumer, type)
        return this
    }
    fun addConsumer(consumer: Consumer<List<LivingEntity>>, type: Type){
        if (type == Type.BENEFICIAL){
            goodConsumers.add(AugmentConsumer(consumer,type))
        } else {
            badConsumers.add(AugmentConsumer(consumer,type))
        }
    }
    fun addConsumers(list: List<AugmentConsumer>){
        list.forEach {
            if (it.type == Type.BENEFICIAL){
                goodConsumers.add(AugmentConsumer(it.consumer,it.type))
            } else {
                badConsumers.add(AugmentConsumer(it.consumer,it.type))
            }
        }
    }
    fun setConsumers(list: MutableList<AugmentConsumer>, type: Type){
        if (type == Type.BENEFICIAL){
            goodConsumers = list
        } else {
            badConsumers = list
        }
    }
    fun setConsumers(ae: AugmentEffect){
        goodConsumers = ae.goodConsumers
        badConsumers = ae.badConsumers
    }
}

data class XpModifiers(var furyXpMod: Int = 0, var witXpMod: Int = 0, var graceXpMod: Int = 0){
    fun plus(xpMods: XpModifiers?){
        if(xpMods == null) return
        this.furyXpMod += xpMods.furyXpMod
        this.witXpMod += xpMods.witXpMod
        this.graceXpMod += xpMods.graceXpMod
    }
    fun getMod(spellKey: String): Int{
        return when(spellKey){
            SpellType.FURY.name ->{this.furyXpMod}
            SpellType.WIT.name ->{this.witXpMod}
            SpellType.GRACE.name ->{this.graceXpMod}
            else -> 0
        }
    }
    fun withFuryMod(furyXpMod: Int = 0): XpModifiers {
        return this.copy(furyXpMod = furyXpMod)
    }
    fun withWitMod(witXpMod: Int = 0): XpModifiers {
        return this.copy(witXpMod = witXpMod)
    }
    fun withGraceMod(graceXpMod: Int = 0): XpModifiers {
        return this.copy(graceXpMod = graceXpMod)
    }
}

data class AugmentConsumer(val consumer: Consumer<List<LivingEntity>>, val type: Type) {
    enum class Type {
        HARMFUL,
        BENEFICIAL
    }
}

