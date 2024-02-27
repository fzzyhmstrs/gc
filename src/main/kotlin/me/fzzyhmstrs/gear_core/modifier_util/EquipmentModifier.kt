package me.fzzyhmstrs.gear_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.SlotId
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierConsumer
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil
import me.fzzyhmstrs.gear_core.GC
import net.minecraft.block.BlockState
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate

open class EquipmentModifier(
    modifierId: Identifier = AbstractModifierHelper.BLANK,
    val target: EquipmentModifierTarget = EquipmentModifierTarget.NONE,
    val weight: Int = 10,
    val rarity: Rarity = Rarity.COMMON,
    private val persistent: Boolean = false,
    private val randomSelectable: Boolean = true): AbstractModifier<EquipmentModifier>(modifierId)
{

    private var customFormatting: Array<out Formatting> = arrayOf()

    open fun randomlySelectable(): Boolean{
        return randomSelectable
    }

    open fun isPersistent(): Boolean{
        return persistent
    }

    open fun getFormatting(): Array<out Formatting> {
        return if (customFormatting.isEmpty()){
            rarity.formatting
        } else {
            customFormatting
        }
    }

    init{
        if (modifierId != AbstractModifierHelper.BLANK)
            EquipmentModifierHelper.addToTargetMap(this)
    }

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifierContainer> = ArrayListMultimap.create()
    private val modifierModifiers: MutableList<Identifier> = mutableListOf()
    private val postHitConsumers: MutableList<ModifierConsumer> = mutableListOf()
    private val postMineConsumers: MutableList<MiningConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<ModifierConsumer> = mutableListOf()
    private val onAttackFunctions: MutableList<DamageFunction> = mutableListOf()
    private val onDamagedFunctions: MutableList<DamageFunction> = mutableListOf()
    private val killOtherConsumers: MutableList<ModifierConsumer> = mutableListOf()
    private val tickConsumers: MutableList<ModifierConsumer> = mutableListOf()
    private var durabilityModifier: PerLvlI = PerLvlI()

    var toll: LootNumberProvider = ConstantLootNumberProvider.create(5f)


    open fun isEnabled(): Boolean{
        return true
    }

    override fun plus(other: EquipmentModifier): EquipmentModifier {
        if (!other.isEnabled()) return this
        attributeModifiers.putAll(other.attributeModifiers)
        modifierModifiers.addAll(other.modifierModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        postMineConsumers.addAll(other.postMineConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        onAttackFunctions.addAll(other.onAttackFunctions)
        onDamagedFunctions.addAll(other.onDamagedFunctions)
        killOtherConsumers.addAll(other.killOtherConsumers)
        tickConsumers.addAll(other.tickConsumers)
        durabilityModifier = durabilityModifier.plus(other.durabilityModifier)
        return this
    }

    fun withCustomFormatting(vararg formatting: Formatting): EquipmentModifier{
        customFormatting = formatting
        return this
    }

    @Deprecated("uuid field no longer necessary, as attributes will be rebuilt with slot-appropriate UUIDs during modifier compilation")
    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): EquipmentModifier {
        val modifier = EntityAttributeModifierContainer(this.getTranslationKey() + "/" + attribute.translationKey,amount,operation, UUID.fromString(uuid))
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, amount: Double, operation: EntityAttributeModifier.Operation): EquipmentModifier {
        val modifier = EntityAttributeModifierContainer(this.getTranslationKey() + "/" + attribute.translationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    open fun attributeModifiers(): Multimap<EntityAttribute, EntityAttributeModifierContainer>{
        return attributeModifiers
    }

    internal fun prepareContainerMap(slot: SlotId?, offset: Int = 0, newMap : Multimap<EntityAttribute, EntityAttributeModifier>){
        for (entry in attributeModifiers().entries()){
            if (slot == null){
                newMap.put(entry.key,entry.value.provideNonSlotEntityAttribute())
                continue
            }
            newMap.put(entry.key,entry.value.provideEntityAttribute(slot, offset))
        }
    }

    fun withModifiers(vararg modifier: Identifier): EquipmentModifier{
        if (modifier.isNotEmpty()) {
            modifierModifiers.addAll(modifier)
        }
        return this
    }

    open fun modifiers(): List<Identifier>{
        return modifierModifiers
    }

    fun withDurabilityMod(durabilityMod: PerLvlI): EquipmentModifier{
        this.durabilityModifier = durabilityMod
        return this
    }

    open fun modifyDurability(durability: Int): Int{
        val dur = PerLvlI(durability)
        //println(dur)
        //println(durabilityModifier)
        val blh = dur.plus(durabilityModifier).value(0)
        //println(blh)
        return blh
    }

    fun withPostHit(onHit: ModifierConsumer): EquipmentModifier {
        postHitConsumers.add(onHit)
        return this
    }

    open fun postHit(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        postHitConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withPostMine(onMine: MiningConsumer): EquipmentModifier {
        postMineConsumers.add(onMine)
        return this
    }

    open fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity){
        postMineConsumers.forEach {
            it.apply(stack, world, state, pos, miner)
        }
    }

    fun withOnUse(onUse: ModifierConsumer): EquipmentModifier {
        onUseConsumers.add(onUse)
        return this
    }

    open fun onUse(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        onUseConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withOnAttack(onAttack: DamageFunction): EquipmentModifier {
        onAttackFunctions.add(onAttack)
        return this
    }

    open fun onAttack(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float{
        var newAmount = amount
        onAttackFunctions.forEach {
            newAmount = it.test(stack, user, attacker, source, newAmount)
        }
        return newAmount
    }

    fun withOnDamaged(onDamaged: DamageFunction): EquipmentModifier {
        onDamagedFunctions.add(onDamaged)
        return this
    }

    open fun onDamaged(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float{
        var newAmount = amount
        //println(newAmount)
        onDamagedFunctions.forEach {
            newAmount = it.test(stack, user, attacker, source, newAmount)
        }
        //println(newAmount)
        return newAmount
    }

    fun withKilledOther(killOther: ModifierConsumer): EquipmentModifier {
        killOtherConsumers.add(killOther)
        return this
    }

    open fun killedOther(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        killOtherConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withTick(tick: ModifierConsumer): EquipmentModifier {
        tickConsumers.add(tick)
        return this
    }

    open fun tick(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        tickConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withDescendant(modifier: EquipmentModifier): EquipmentModifier {
        addDescendant(modifier)
        return this
    }

    fun withToll(toll: LootNumberProvider): EquipmentModifier{
        this.toll = toll
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(arrayListOf(), EquipmentModifier())
    }

    override fun getModifierHelper(): AbstractModifierHelper<EquipmentModifier> {
        return EquipmentModifierHelper
    }

    override fun toString(): String {
        return "{Equipment Modifier: $modifierId}"
    }

    @FunctionalInterface
    fun interface MiningConsumer{
        fun apply(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity)
    }

    @FunctionalInterface
    fun interface DamageFunction{
        fun test(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float
    }

    abstract class EquipmentModifierTarget(val id: Identifier){

        companion object{

            val CODEC: Codec<EquipmentModifierTarget> = Identifier.CODEC.comapFlatMap(
                {id ->
                    for (target in targets){
                        if (target.id == id)
                            DataResult.success(target)
                    }
                    DataResult.error { "EquipmentModifierTarget $id not found in instantiated Targets list" }
                },
                {t: EquipmentModifierTarget -> t.id }
            )

            val GLOBAL_EXCLUSIONS: TagKey<Item> =
                TagKey.of(
                    RegistryKeys.ITEM,
                    Identifier(
                        GC.MOD_ID,
                        "global_modifier_exclusions")
                )
            internal val targets: MutableList<EquipmentModifierTarget> = mutableListOf()
            val trinketsPredicate by lazy {
                getTrinketCheck()
            }

            fun getTrinketCheck(): Predicate<ItemStack>{
                return TrinketUtil.trinketCheck
            }

            internal fun findTargetForItem(stack: ItemStack): List<EquipmentModifierTarget>{
                val list: MutableList<EquipmentModifierTarget> = mutableListOf()
                for (target in targets){
                    if (target.isStackAcceptable(stack)){
                        list.add(target)
                    }
                }
                return list
            }

            val NONE = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"none")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return false
                }
            }
            val ANY = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"any")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return TOOL.isItemAcceptableOrTagged(stack) || ARMOR.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val BREAKABLE = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"breakable")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item.isDamageable
                }
            }
            val ANY_WEAPON = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"any_weapon")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return WEAPON.isItemAcceptableOrTagged(stack) || RANGED.isItemAcceptableOrTagged(stack)
                }
            }
            val WEAPON = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"weapon")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return SWORD.isItemAcceptableOrTagged(stack) || AXE.isItemAcceptableOrTagged(stack) || TRIDENT.isItemAcceptableOrTagged(stack)
                }
            }
            val WEAPON_AND_TRINKET = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"weapon_and_trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return WEAPON.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val SWORD = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"sword")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is SwordItem
                }
            }
            val AXE = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"axe")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is AxeItem
                }
            }
            val RANGED = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"ranged")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return TRIDENT.isItemAcceptableOrTagged(stack) || BOW.isItemAcceptableOrTagged(stack)
                }
            }
            val TRIDENT = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"trident")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is TridentItem
                }
            }
            val BOW = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"bow")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is RangedWeaponItem
                }
            }
            val MINING = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"mining")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is MiningToolItem
                }
            }
            val SHIELD = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"shield")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ShieldItem
                }
            }
            val TOOL = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"tool")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ToolItem
                }
            }
            val ARMOR = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return stack.item is ArmorItem
                }
            }
            val ARMOR_AND_TRINKET = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor_and_trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return ARMOR.isItemAcceptableOrTagged(stack) || TRINKET.isItemAcceptableOrTagged(stack)
                }
            }
            val ARMOR_HEAD = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor_head")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.HEAD)
                }
            }
            val ARMOR_CHEST = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor_chest")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.CHEST)
                }
            }
            val ARMOR_LEGS = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor_legs")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.LEGS)
                }
            }
            val ARMOR_FEET = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"armor_feet")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    val item = stack.item
                    return (item is ArmorItem && item.slotType == EquipmentSlot.FEET)
                }
            }
            val TRINKET = object: EquipmentModifierTarget(Identifier(GC.MOD_ID,"trinket")){
                override fun isAcceptableItem(stack: ItemStack): Boolean{
                    return if (TrinketChecker.trinketsLoaded) {
                        trinketsPredicate.test(stack)
                    } else {
                        false
                    }
                }
            }
        }

        val tagIncluded: TagKey<Item>
        val tagExcluded: TagKey<Item>

        init{
            for (target in targets){
                if (target.id == id) throw IllegalStateException("Equipment Modifier target $id already instantiated!")
            }
            targets.add(getTarget())
            tagIncluded = TagKey.of(RegistryKeys.ITEM,id)
            tagExcluded = TagKey.of(RegistryKeys.ITEM,Identifier(id.namespace,id.path + "_excluded"))
        }

        private fun getTarget(): EquipmentModifierTarget{
            return this
        }

        override fun equals(other: Any?): Boolean{
            if (other == null) return false
            if (other !is EquipmentModifierTarget) return false
            return other.id == id
        }

        override fun hashCode(): Int{
            return id.hashCode() + 31 * id.hashCode()
        }

        fun isStackAcceptable(stack: ItemStack): Boolean{
            if (stack.isIn(GLOBAL_EXCLUSIONS)) return false
            return isItemAcceptableOrTagged(stack)
        }

        @Deprecated("Use only in new target instances. Call isStackAcceptable for outside usage.")
        fun isItemAcceptableOrTagged(stack: ItemStack): Boolean{
            if (stack.isIn(tagExcluded)) return false
            return stack.isIn(tagIncluded) || isAcceptableItem(stack)
        }

        protected abstract fun isAcceptableItem(stack: ItemStack): Boolean

    }

    enum class Rarity(val beneficial: Boolean,vararg val formatting: Formatting): StringIdentifiable{
        REALLY_BAD(false, Formatting.BOLD, Formatting.DARK_RED),
        BAD(false, Formatting.DARK_RED),
        COMMON(true, Formatting.GRAY),
        UNCOMMON(true, Formatting.DARK_GREEN),
        RARE(true, Formatting.AQUA),
        EPIC(true, Formatting.LIGHT_PURPLE),
        LEGENDARY(true, Formatting.BOLD, Formatting.GOLD);

        override fun asString(): String {
            return name
        }

        companion object{
            val CODEC = StringIdentifiable.createCodec{ Rarity.values() }
        }
    }

}