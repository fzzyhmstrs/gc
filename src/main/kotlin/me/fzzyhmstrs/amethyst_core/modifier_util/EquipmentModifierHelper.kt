package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

object EquipmentModifierHelper: AbstractModifierHelper<EquipmentModifier>() {

    private val targetMap: MultiMap<EquipmentModifierTarget, EquipmentModifier> = ArrayListMultiMap.create()
    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(5,0.5f)
    private val BLANK_WEAPON_MOD = EquipmentModifier(ModifierDefaults.BLANK_ID)
    private val tooltips: Map<Long, List<Text>> = mutableMapOf()
    
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
                list.add(AcText.translatable(it.getTranslationKey()).formatted(it.rarity.formatting)
                            .append(AcText.literal(" - ").formatted(it.rarity.formatting)
                            .append(AcText.translatable(it.getDescTranslationKey()).formatted(it.rarity.formatting).formatted(Formatting.ITALIC)
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
        tooltip.addAll(tooltips)
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
        for (i = 1..weight){
            targetMap.put(target,modifier)
        }
    }
    
    fun addRandomModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL){
        val target = EquipmentModifierTarget.findTargetForItem(stack)
        if (target == null) return
        val list = targetMap.get(target)
        if (list == null) return
        var tollRemaining = toll.nextFloat(context).toInt()
        while (tollRemaining > 0){
            val modChk = list.get(context.getRandom().nextInt(list.size))
            EquipmentModifierHelper.addModifier
            tollRemaining -= modChk.toll.nextFloat(context).toInt()
        }
    }
    
    fun rerollModifiers(stack: ItemStack, context: LootContext, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL){
        val modifiers = getModifiers(stack)
        val nbt = stack.orCreateNbt
        for (id: modifiers){
            val mod = ModifierRegistry.getByType<EquipmentModifier>(id)
            if (mod?.persistent == true) continue
            removeModifier(stack, id, nbt)
        }
        addRandomModifiers(stack, context, toll)
    }

}
