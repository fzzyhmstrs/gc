package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketComponent
import dev.emi.trinkets.api.TrinketsApi
import me.fzzyhmstrs.amethyst_core.coding_util.AcText
import me.fzzyhmstrs.amethyst_core.interfaces.AugmentTracking
import me.fzzyhmstrs.amethyst_core.interfaces.DurabilityTracking
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*
import kotlin.collections.ArrayList

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val targetMap: Multimap<EquipmentModifier.EquipmentModifierTarget, EquipmentModifier> = ArrayListMultimap.create()
    private val attributeMap: MutableMap<Long, Multimap<EntityAttribute, EntityAttributeModifier>> = mutableMapOf()
    private val augmentMap: MutableMap<UUID, AbstractModifier.CompiledModifiers<AugmentModifier>> = mutableMapOf()
    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(10,0.5f)
    private val BLANK_WEAPON_MOD = EquipmentModifier(ModifierDefaults.BLANK_ID)
    private val tooltips: MutableMap<Long, List<Text>> = mutableMapOf()
    
    override val fallbackData: AbstractModifier.CompiledModifiers<EquipmentModifier>
        get() = AbstractModifier.CompiledModifiers(listOf(), EquipmentModifier(ModifierDefaults.BLANK_ID))

    override fun gatherActiveModifiers(stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            val compiled = gatherActiveAbstractModifiers(
                stack,
                ModifierDefaults.BLANK_ID,
                BLANK_WEAPON_MOD.compiler()
            )
            val list: MutableList<Text> = mutableListOf()
            compiled.modifiers.forEach {
                list.add(
                    AcText.translatable(it.getTranslationKey()).formatted(*it.rarity.formatting)
                        .append(AcText.literal(" - ").formatted(*it.rarity.formatting))
                        .append(
                            AcText.translatable(it.getDescTranslationKey()).formatted(*it.rarity.formatting)
                                .formatted(Formatting.ITALIC)
                        )
                )
            }
            tooltips[id] = list
            setModifiersById(
                id,
                compiled
            )
            (stack as DurabilityTracking).evaluateNewMaxDamage(compiled)
            val map: Multimap<EntityAttribute, EntityAttributeModifier> = ArrayListMultimap.create()
            map.putAll(compiled.compiledData.attributeModifiers())
            for (slot in EquipmentSlot.values()){
                val stackMap = stack.getAttributeModifiers(slot)
                if (!stackMap.isEmpty){
                    map.putAll(stackMap)
                    break
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
    
    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>){
        val id = Nbt.makeItemStackId(stack)
        tooltip.addAll(tooltips[id]?: listOf())
    }

    fun modifyCompiledAugmentModifiers(original: AbstractModifier.CompiledModifiers<AugmentModifier>, uuid: UUID): AbstractModifier.CompiledModifiers<AugmentModifier>{
        val augments = augmentMap[uuid]?:return original
        val list: MutableList<AugmentModifier> = mutableListOf()
        list.addAll(augments.modifiers)
        list.addAll(original.modifiers)
        return AbstractModifier.CompiledModifiers(list,AugmentModifier().plus(augments.compiledData).plus(original.compiledData))
    }

    fun processEquipmentAugmentModifiers(stack: ItemStack, entity: LivingEntity){
        val item = stack.item
        if (item !is AugmentTracking) return
        val uuid = entity.uuid
        val list: MutableList<AugmentModifier> = mutableListOf()
        val optional: Optional<TrinketComponent>  = TrinketsApi.getTrinketComponent(entity)
        if (optional.isPresent) {
            val stacks = optional.get().allEquipped
            for (entry in stacks) {
                val chk = entry.right.item
                if (chk is AugmentTracking) {
                    list.addAll(chk.getModifiers(entry.right))
                }
            }
        }
        for(armor in entity.armorItems) {
            val chk = armor.item
            if (chk is AugmentTracking){
                list.addAll(chk.getModifiers(armor))
            }
        }
        val mainhand = entity.getEquippedStack(EquipmentSlot.MAINHAND)
        val chk2 = mainhand.item
        if (chk2 is AugmentTracking){
            list.addAll(chk2.getModifiers(mainhand))
        }
        val offhand = entity.getEquippedStack(EquipmentSlot.OFFHAND)
        val chk3 = offhand.item
        if (chk3 is AugmentTracking){
            list.addAll(chk3.getModifiers(offhand))
        }
        if (list.isNotEmpty()){
            val compiler = ModifierDefaults.BLANK_AUG_MOD.compiler()
            list.forEach {
                compiler.add(it)
            }
            augmentMap[uuid] = compiler.compile()
        }
    }

    fun getAttributeModifiers(stack: ItemStack, slot: EquipmentSlot): Multimap<EntityAttribute, EntityAttributeModifier> {
        val id = Nbt.getItemStackId(stack)
        return attributeMap[id]?:stack.getAttributeModifiers(slot)
    }
    
    fun addUniqueModifier(modifier: Identifier, stack: ItemStack){
        if (checkDescendant(modifier,stack) == null){
            addModifier(modifier, stack)
        }
    }
    
    internal fun addToTargetMap(modifier: EquipmentModifier){
        if (!modifier.randomSelectable) return
        val target = modifier.target
        val weight = modifier.weight
        for (i in 1..weight){
            targetMap.put(target,modifier)
        }
    }
    
    fun addRandomModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL){
        val targetList = EquipmentModifier.EquipmentModifierTarget.findTargetForItem(stack)
        if (targetList.isEmpty()) return
        val list: ArrayList<EquipmentModifier> = ArrayList()
        for (target in targetList){
            list.addAll(targetMap.get(target).toList())
        }
        var tollRemaining = (toll.nextFloat(context) + context.luck).toInt()
        while (tollRemaining > 0){
            val modChk = list[context.random.nextInt(list.size)]
            tollRemaining -= modChk.toll.nextFloat(context).toInt()
            if (tollRemaining >= 0) {
                addUniqueModifier(modChk.modifierId,stack)
            }
        }
    }
    
    fun rerollModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL){
        val modifiers = getModifiers(stack)
        val nbt = stack.orCreateNbt
        for (id in modifiers){
            val mod = ModifierRegistry.getByType<EquipmentModifier>(id)
            if (mod?.persistent == true) continue
            removeModifier(stack, id, nbt)
        }
        addRandomModifiers(stack, context, toll)
    }

    override fun getModifierByType(id: Identifier): EquipmentModifier? {
        return ModifierRegistry.getByType<EquipmentModifier>(id)
    }

}
