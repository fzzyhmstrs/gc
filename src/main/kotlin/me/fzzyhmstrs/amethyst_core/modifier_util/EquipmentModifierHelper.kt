package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.coding_util.AcText
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val targetMap: Multimap<EquipmentModifier.EquipmentModifierTarget, EquipmentModifier> = ArrayListMultimap.create()
    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(10,0.5f)
    private val BLANK_WEAPON_MOD = EquipmentModifier(ModifierDefaults.BLANK_ID)
    private val tooltips: MutableMap<Long, List<Text>> = mutableMapOf()
    
    override val fallbackData: AbstractModifier<EquipmentModifier>.CompiledModifiers
        get() = BLANK_WEAPON_MOD.CompiledModifiers(listOf(), EquipmentModifier(ModifierDefaults.BLANK_ID))

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
            compiled.modifiers.forEach{
                list.add(
                    AcText.translatable(it.getTranslationKey()).formatted(*it.rarity.formatting)
                            .append(AcText.literal(" - ").formatted(*it.rarity.formatting))
                            .append(AcText.translatable(it.getDescTranslationKey()).formatted(*it.rarity.formatting).formatted(Formatting.ITALIC))
                )
            }
            tooltips[id] = list
            setModifiersById(
                id,
                compiled
            )
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
        if (targetList.isEmpty) return
        val list: ArrayList<EquipmentModifier> = ArrayList()
        for (target in targetList){
            list.addAll(targetMap.get(target).toList()
        }
        var tollRemaining = (toll.nextFloat(context) + context.getLuck()).toInt()
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
