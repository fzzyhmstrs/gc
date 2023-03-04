package me.fzzyhmstrs.gear_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.gear_core.interfaces.AttributeTracking
import me.fzzyhmstrs.gear_core.interfaces.DurabilityTracking
import me.fzzyhmstrs.gear_core.trinkets.TrinketsUtil
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
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
    private val ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF")
    private val ATTACK_SPEED_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3")
    
    override val fallbackData: AbstractModifier.CompiledModifiers<EquipmentModifier>
        get() = AbstractModifier.CompiledModifiers(listOf(), EquipmentModifier(BLANK))

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
    
    private fun prepareAttributeMapForSlot(stack: ItemStack, map: Multimap<EntityAttribute, EntityAttributeModifier>): Multimap<EntityAttribute, EntityAttributeModifier>{
        val item = stack.item
        if (item !is AttributeTracking) return EMPTY_ATTRIBUTE_MAP
        val slot = item.correctSlot
        val stackMap = if(slot == null){
            EMPTY_ATTRIBUTE_MAP
        } else {
            item.getAttributeModifiers(slot)
        }
        if (map.isEmpty) return stackMap
        val newMap: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create(stackMap)
        newMap.putAll(randomize(map))
        return newMap
    }

    private fun randomize(map: Multimap<EntityAttribute, EntityAttributeModifier>): Multimap<EntityAttribute, EntityAttributeModifier>{
        val newMap : Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
        for (entry in map.entries()){
            val uuid = UUID.randomUUID()
            val name = entry.value.name
            val amount1 = entry.value.value
            val operation = entry.value.operation
            newMap.put(entry.key,EntityAttributeModifier(uuid, name, amount1,operation))
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
        //println(map)
        return attributeMap[id]?:original
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

    fun addRandomModifiers(stack: ItemStack, context: LootContext){
        addRandomModifiers(stack, context, DEFAULT_MODIFIER_TOLL)
    }
    
    fun addRandomModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider){
        val targetList = EquipmentModifier.EquipmentModifierTarget.findTargetForItem(stack)
        if (targetList.isEmpty()) return
        val list: ArrayList<EquipmentModifier> = ArrayList()
        for (target in targetList){
            //println(target.id)
            list.addAll(targetMap.get(target))
        }
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
        val contextBuilder = LootContext.Builder(world).random(world.random).luck(player.luck)
        addRandomModifiers(stack,contextBuilder.build(LootContextTypes.EMPTY))
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
}
