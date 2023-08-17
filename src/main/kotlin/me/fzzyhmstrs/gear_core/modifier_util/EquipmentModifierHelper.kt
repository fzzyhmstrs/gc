package me.fzzyhmstrs.gear_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.gear_core.GC
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking
import me.fzzyhmstrs.gear_core.interfaces.DurabilityTracking
import me.fzzyhmstrs.gear_core.trinkets.TrinketsUtil
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val targetMap: ArrayListMultimap<EquipmentModifier.EquipmentModifierTarget, EquipmentModifier> = ArrayListMultimap.create()
    private val attributeMap: MutableMap<Long, Multimap<EntityAttribute, EntityAttributeModifier>> = mutableMapOf()
    private val processors: MutableList<ModifierProcessor> = mutableListOf()

    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(25,0.24f)
    private val BLANK_EQUIPMENT_MOD = EquipmentModifier(BLANK)
    private val EMPTY_ATTRIBUTE_MAP: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
    private const val OLD_MODIFIERS_KEY = "modifiers"
    private const val OLD_MODIFIER_ID_KEY = "modifier_id"
    
    override val fallbackData: AbstractModifier.CompiledModifiers<EquipmentModifier>
        get() = AbstractModifier.CompiledModifiers(arrayListOf(), EquipmentModifier(BLANK))

    override fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, list: List<Identifier>) {
        fixUpOldNbt(nbt)
        super.initializeModifiers(stack, nbt, list)
    }

    // attempt to carry over modifiers stored under the now non-generic nbt keys into the new keys
    private fun fixUpOldNbt(nbt: NbtCompound){
        if (nbt.contains(OLD_MODIFIERS_KEY)){
            val oldNbtList = nbt.getList(OLD_MODIFIERS_KEY,10)
            val newNbtList = NbtList()
            val newOldNbtList = NbtList()
            for (el in oldNbtList){
                val compound = el as NbtCompound
                if (compound.contains(OLD_MODIFIER_ID_KEY)){
                    val modifier = compound.getString(OLD_MODIFIER_ID_KEY)
                    if (getModifierByType(Identifier(modifier)) != null){
                        val newEl = NbtCompound()
                        newEl.putString(getType().getModifierIdKey(),modifier)
                        newNbtList.add(newEl)
                    } else {
                        newOldNbtList.add(el.copy())
                    }
                }
            }
            nbt.remove(OLD_MODIFIERS_KEY)
            if (newOldNbtList.isNotEmpty()){
                nbt.put(OLD_MODIFIERS_KEY,newOldNbtList)
            }
            if (newNbtList.isNotEmpty()){
                nbt.put(getType().getModifiersKey(), newNbtList)
            }
        }
    }

    override fun gatherActiveModifiers(stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            val compiled = gatherActiveAbstractModifiers(
                stack,
                BLANK,
                BLANK_EQUIPMENT_MOD.compiler()
            )
            setModifiersById(
                id,
                compiled
            )
            prepareActiveModifierData(stack,nbt, compiled, id)
        }
    }

    private fun prepareActiveModifierData(stack: ItemStack, nbt: NbtCompound, compiled: AbstractModifier.CompiledModifiers<EquipmentModifier>,id: Long){
        (stack as DurabilityTracking).evaluateNewMaxDamage(compiled)
        attributeMap.remove(id)
        val map: Multimap<EntityAttribute, EntityAttributeModifier> = prepareAttributeMapForSlot(stack, ArrayListMultimap.create(compiled.compiledData.attributeModifiers()))
        if (TrinketChecker.trinketsLoaded){
            TrinketsUtil.addTrinketNbt(stack,nbt,map)
        }
        attributeMap[id] = map
    }
    
    private fun prepareAttributeMapForSlot(stack: ItemStack, map: Multimap<EntityAttribute, EntityAttributeModifierContainer>): Multimap<EntityAttribute, EntityAttributeModifier>{
        val item = stack.item
        if (item !is AttributeTracking) return EMPTY_ATTRIBUTE_MAP
        val slot = item.correctSlot
/*        val stackMap = if(slot == null){
            EMPTY_ATTRIBUTE_MAP
        } else {
            item.getAttributeModifiers(slot)
        }*/
        if (map.isEmpty) return EMPTY_ATTRIBUTE_MAP
        /*val newMap: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
        newMap.putAll(randomize(map))*/
        return prepareContainerMap(slot,map)
    }

    internal fun prepareContainerMap(slot: EquipmentSlot?,map: Multimap<EntityAttribute, EntityAttributeModifierContainer>): Multimap<EntityAttribute, EntityAttributeModifier>{
        val newMap : Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
        for (entry in map.entries()){
            if (slot == null){
                newMap.put(entry.key,entry.value.provideNonSlotEntityAttribute())
                continue
            }
            newMap.put(entry.key,entry.value.provideEntityAttribute(slot))
        }
        return newMap
    }

    override fun getTranslationKeyFromIdentifier(id: Identifier): String {
        return "equipment.modifier.${id}"
    }
    
    override fun getDescTranslationKeyFromIdentifier(id: Identifier): String {
        return "equipment.modifier.${id}.desc"
    }
    
    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val nbt = stack.nbt ?: return
        val id = Nbt.getItemStackId(nbt)
        if (id == -1L) return
        val compiled = getActiveModifiers(stack)
        if(getModifiersById(id).isNotEmpty() && compiled.modifiers.isEmpty()){
            gatherActiveModifiers(stack)
        }
        val modifierList = getModifiers(stack)
        if (modifierList.isEmpty()) return
        for (it in modifierList) {
            val mod = getModifierByType(it) ?: continue
            tooltip.add(
                AcText.translatable("gear_core.modifier.colon", AcText.translatable(getTranslationKeyFromIdentifier(it)).string, AcText.translatable(
                    getDescTranslationKeyFromIdentifier(it)
                ).formatted(Formatting.ITALIC)).formatted(*mod.rarity.formatting)
            )
        }
    }

    override fun getModifierByType(id: Identifier): EquipmentModifier? {
        return ModifierRegistry.getByType<EquipmentModifier>(id)
    }

    fun getAttributeModifiers(stack: ItemStack, original: Multimap<EntityAttribute, EntityAttributeModifier>): Multimap<EntityAttribute, EntityAttributeModifier> {
        val id = Nbt.getItemStackId(stack)
        if (id == -1L){
            val nbt = stack.nbt
            if (nbt != null){
                if (nbt.contains(getType().getModifiersKey())){
                    if (nbt.getList(getType().getModifiersKey(),10).isNotEmpty()){
                        gatherActiveModifiers(stack)
                    }
                } else {
                    return original
                }
            }else {
                return original
            }
        } else if (!attributeMap.containsKey(id)){
            gatherActiveModifiers(stack)
        }
        //println(map)
        val modMap = attributeMap[id]?: return original
        if (modMap.isEmpty) return original
        val newMap:Multimap<EntityAttribute, EntityAttributeModifier>  = ArrayListMultimap.create()
        newMap.putAll(original)
        newMap.putAll(modMap)
        return newMap
    }

    fun addUniqueModifier(modifier: Identifier, stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt == null){
            addModifier(modifier, stack)
            return
        }
        val id = Nbt.getItemStackId(nbt)
        if (!(id != -1L && checkDescendant(modifier, stack) != null)) {
            addModifier(modifier, stack)
        }
    }
    
    internal fun addToTargetMap(modifier: EquipmentModifier){
        if (!modifier.randomlySelectable()) return
        val target = modifier.target
        val weight = modifier.weight
        for (i in 1..weight){
            targetMap.put(target,modifier)
        }
    }

    fun getTargetsForItem(stack: ItemStack): ArrayList<EquipmentModifier>{
        val targetList = EquipmentModifier.EquipmentModifierTarget.findTargetForItem(stack)
        if (targetList.isEmpty()) return arrayListOf()
        val list: ArrayList<EquipmentModifier> = ArrayList()
        for (target in targetList){
            //println(target.id)
            list.addAll(targetMap.get(target))
        }
        return list
    }

    fun addModifierAsIs(modifier: Identifier, stack: ItemStack, temporary: Boolean = false){
        val id = Nbt.makeItemStackId(stack)
        val nbt = stack.orCreateNbt
        addModifierWithoutChecking(id, modifier, stack, nbt, temporary)
    }

    fun addRandomModifiers(stack: ItemStack, context: LootContext){
        addRandomModifiers(stack, context, DEFAULT_MODIFIER_TOLL)
    }
    
    fun addRandomModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider){
        val list = getTargetsForItem(stack)
        if (list.isEmpty()) return
        var tollRemaining = (toll.nextFloat(context) + context.luck).toInt()
        //println("toll: $tollRemaining")
        //println("targets: $list")
        while (tollRemaining > 0){
            val modChk = list[context.random.nextInt(list.size)]
            //println("next try: $modChk")
            tollRemaining -= modChk.toll.nextFloat(context).toInt()
            //println("remaining toll: $tollRemaining")
            if (tollRemaining >= 0) {
                addUniqueModifier(modChk.modifierId,stack)
                //println("Added try!")
            }
        }
    }

    fun rerollModifiers(stack: ItemStack, world: ServerWorld, player: PlayerEntity){
        val nbt = stack.orCreateNbt
        removeNonPersistentModifiers(stack)
        nbt.remove(NbtKeys.ITEM_STACK_ID.str())
        val parameters = LootContextParameterSet.Builder(world).luck(player.luck).build(LootContextTypes.EMPTY)
        val seed = world.random.nextLong().takeIf { it != 0L }?:1L
        val contextBuilder = LootContext.Builder(parameters).random(seed)
        addRandomModifiers(stack,contextBuilder.build(null))
    }

    fun removeModifier(modifier: Identifier,stack: ItemStack){
        val nbt = stack.nbt ?: return
        removeModifier(stack, modifier, nbt)
    }

    private fun removeNonPersistentModifiers(stack: ItemStack){
        val nbt = stack.orCreateNbt
        val modList = getModifiersFromNbt(stack)
        for (id in modList){
            val mod = getModifierByType(id) ?: continue
            if (mod.isPersistent()) continue
            removeModifierFromNbt(id,nbt)
        }
    }

    fun processModifiers(stack: ItemStack, entity: LivingEntity){
        processors.forEach {
            it.process(stack, entity)
        }
    }

    fun registerModifierProcessor(processor: ModifierProcessor){
        processors.add(processor)
    }

    @FunctionalInterface
    fun interface ModifierProcessor{
        fun process(stack: ItemStack, entity: LivingEntity)
    }

    override fun getType(): ModifierHelperType {
        return GC.EQUIPMENT_MODIFIER_TYPE
    }
}
