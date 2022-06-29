package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.coding_util.TickingDustbin
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import kotlin.math.max

object ModifierHelper {

    private val augmentModifiers: MutableMap<ItemStack ,MutableList<Identifier>> = mutableMapOf()
    private val activeScepterModifiers: MutableMap<ItemStack, AbstractModifier<AugmentModifier>.CompiledModifiers> = mutableMapOf()
    internal val DUSTBIN = TickingDustbin { dirt: ItemStack -> gatherActiveScepterModifiers(dirt) }

    fun addModifier(modifier: Identifier, stack: ItemStack): Boolean{
        val nbt = stack.orCreateNbt
        return addModifier(modifier, stack, nbt)
    }
    fun addModifierForREI(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierToNbt(modifier, nbt)
    }
    private fun addModifier(modifier: Identifier, scepter: ItemStack, nbt: NbtCompound): Boolean{
        if (!augmentModifiers.containsKey(scepter)) {
            augmentModifiers[scepter] = mutableListOf()
        }
        val highestModifier = checkDescendant(modifier,scepter)
        if (highestModifier != null){
            val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
            return if (mod?.hasDescendant() == true){
                val highestDescendantPresent: Int = checkModifierLineage(mod,scepter)
                if (highestDescendantPresent < 0){
                    false
                } else {
                    val lineage = mod.getModLineage()
                    val newDescendant = lineage[highestDescendantPresent]
                    val currentGeneration = lineage[max(highestDescendantPresent - 1,0)]
                    augmentModifiers[scepter]?.add(newDescendant)
                    addModifierToNbt(newDescendant, nbt)
                    removeModifier(scepter, currentGeneration, nbt)
                    DUSTBIN.markDirty(scepter)
                    true
                }
            } else {
                false
            }
        }
        augmentModifiers[scepter]?.add(modifier)
        addModifierToNbt(modifier, nbt)
        DUSTBIN.markDirty(scepter)
        return true

    }
    private fun checkDescendant(modifier: Identifier, scepter: ItemStack): Identifier?{
        val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
        val lineage = mod?.getModLineage() ?: return modifier
        var highestModifier: Identifier? = null
        lineage.forEach { identifier ->
            if (augmentModifiers[scepter]?.contains(identifier) == true){
                highestModifier = identifier
            }
        }
        return highestModifier
    }
    private fun removeModifier(scepter: ItemStack, modifier: Identifier, nbt: NbtCompound){
        augmentModifiers[scepter]?.remove(modifier)
        DUSTBIN.markDirty(scepter)
        removeModifierFromNbt(modifier,nbt)
    }
    private fun addModifierToNbt(modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(NbtKeys.MODIFIER_ID.str(),modifier.toString())
        Nbt.addNbtToList(newEl, NbtKeys.MODIFIERS.str(),nbt)
    }
    private fun removeModifierFromNbt(modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(NbtKeys.MODIFIERS.str(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(NbtKeys.MODIFIER_ID.str())){
                val chk = Identifier(nbtEl.getString(NbtKeys.MODIFIER_ID.str()))
                chk == modifier
            } else {
                false
            }
        }
    }

    fun initializeModifiers(stack: ItemStack, scepterNbt: NbtCompound){
        if (scepterNbt.contains(NbtKeys.MODIFIERS.str())){
            initializeModifiers(scepterNbt, stack)
        }
        DUSTBIN.markDirty(stack)
        DUSTBIN.clean()
    }
    private fun initializeModifiers(nbt: NbtCompound, stack: ItemStack){
        val nbtList = nbt.getList(NbtKeys.MODIFIERS.str(),10)
        for (el in nbtList){
            val compound = el as NbtCompound
            if (compound.contains(NbtKeys.MODIFIER_ID.str())){
                val modifier = compound.getString(NbtKeys.MODIFIER_ID.str())
                addModifier(Identifier(modifier),stack)
            }
        }
        initializeForAttunedEnchant(stack, stack, nbt)
    }

    @Deprecated("Removing after modifiers are released for long enough. Target end of 2022.")
    private fun initializeForAttunedEnchant(stack: ItemStack, id: ItemStack, nbt: NbtCompound){
        if (stack.hasEnchantments()){
            val enchants = stack.enchantments
            var attunedLevel = 0
            var nbtEl: NbtCompound
            for (el in enchants) {
                nbtEl = el as NbtCompound
                if (EnchantmentHelper.getIdFromNbt(nbtEl) == Identifier("amethyst_imbuement","attuned")){
                    attunedLevel = EnchantmentHelper.getLevelFromNbt(nbtEl)
                    break
                }
            }
            if (attunedLevel > 0) {
                for (i in 1..attunedLevel) {
                    addModifier(ModifierRegistry.LESSER_ATTUNED.modifierId, id, nbt)
                }
                val newEnchants = EnchantmentHelper.fromNbt(enchants)
                EnchantmentHelper.set(newEnchants,stack)
            }
        }
    }

    fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.orCreateNbt
        if (!augmentModifiers.containsKey(stack)) {
            if (stack.nbt?.contains(NbtKeys.MODIFIERS.str()) == true) {
                initializeModifiers(nbt, stack)
            }
        }
        return augmentModifiers[stack] ?: listOf()
    }

    fun getActiveModifiers(stack: ItemStack): AbstractModifier<AugmentModifier>.CompiledModifiers {
        return activeScepterModifiers[stack] ?: ModifierDefaults.BLANK_COMPILED_DATA
    }

    fun checkModifierLineage(modifier:Identifier, stack: ItemStack): Boolean{
        val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
        return if (mod != null){
            checkModifierLineage(mod, stack) > 0
        } else {
            false
        }
    }

    private fun checkModifierLineage(mod: AugmentModifier, stack: ItemStack): Int{
        val lineage = mod.getModLineage()
        val highestOrderDescendant = lineage.size
        var highestDescendantPresent = 1
        lineage.forEachIndexed { index, identifier ->
            if (augmentModifiers[stack]?.contains(identifier) == true){
                highestDescendantPresent = index + 1
            }
        }
        return if(highestDescendantPresent < highestOrderDescendant){
            highestDescendantPresent
        } else {
            -1
        }
    }

    private fun gatherActiveScepterModifiers(scepter: ItemStack){
        val nbt = scepter.orCreateNbt
        if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) return
        val activeEnchant =  Identifier(Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(),nbt))
        val compiler = ModifierDefaults.BLANK_AUG_MOD.compiler()
        augmentModifiers[scepter]?.forEach { identifier ->
            val modifier = ModifierRegistry.getByType<AugmentModifier>(identifier)
            if (modifier != null){
                if (!modifier.hasSpellToAffect()){
                    compiler.add(modifier)
                } else {
                    if (modifier.checkSpellsToAffect(activeEnchant)){
                        compiler.add(modifier)
                    }
                }
            }
        }

        activeScepterModifiers[scepter] = compiler.compile()
    }

}