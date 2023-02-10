package me.fzzyhmstrs.gear_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import dev.emi.trinkets.api.Trinket
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.gear_core.interfaces.DurabilityTracking
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val targetMap: ArrayListMultimap<EquipmentModifier.EquipmentModifierTarget, EquipmentModifier> = ArrayListMultimap.create()
    private val attributeMap: MutableMap<Long, Multimap<EntityAttribute, EntityAttributeModifier>> = mutableMapOf()
    private val processors: MutableList<ModifierProcessor> = mutableListOf()
    private var lastClientInit = 0L

    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(25,0.24f)
    private val BLANK_EQUIPMENT_MOD = EquipmentModifier(BLANK)
    private val EMPTY_ATTRIBUTE_MAP: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
    
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
            //println("modifying damage!")
            //println(stack.maxDamage)
            (stack as DurabilityTracking).evaluateNewMaxDamage(compiled)
            //println(stack.maxDamage)

            attributeMap.remove(id)
            val map: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
            map.putAll(compiled.compiledData.attributeModifiers())
            if (stack.item is Trinket){
                println("Adding nbt to trinket")
                nbt.remove("TrinketAttributeModifiers")
                for (entry in map.entries()) {
                    val attribute = entry.key
                    val modifier = entry.value
                    val compound = modifier.toNbt()
                    compound.putString("AttributeName", Registries.ATTRIBUTE.getId(attribute).toString())
                    Nbt.addNbtToList(compound,"TrinketAttributeModifiers",nbt)
                }
            }
            attributeMap[id] = map
        }
    }

    override fun getTranslationKeyFromIdentifier(id: Identifier): String {
        return "equipment.modifier.${id}"
    }
    
    override fun getDescTranslationKeyFromIdentifier(id: Identifier): String {
        return "equipment.modifier.${id}.desc"
    }
    
    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val modifierList = getModifiers(stack)
        if (System.currentTimeMillis() - lastClientInit > 100L){
            lastClientInit = System.currentTimeMillis()
            val compiled = getActiveModifiers(stack)
            (stack as DurabilityTracking).evaluateNewMaxDamage(compiled)
        }
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
        val map: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
        map.putAll(original)
        val id = Nbt.getItemStackId(stack)
        map.putAll(attributeMap[id]?:EMPTY_ATTRIBUTE_MAP)
        return map
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
    
    fun removeNonPersistentModifiers(stack: ItemStack){
        val nbt = stack.orCreateNbt
        val modList = getModifiers(stack)
        for (id in modList){
            val mod = getModifierByType(id)
            if (mod == null) continue
            if (mod.isPersistent()) continue
            removeModifier(stack,id,nbt)
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
