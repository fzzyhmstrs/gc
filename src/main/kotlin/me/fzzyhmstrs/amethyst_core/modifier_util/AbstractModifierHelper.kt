package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.coding_util.TickingDustbin
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import kotlin.math.max

abstract class AbstractModifierHelper<T: AbstractModifier<T>> {

    private val modifiers: MutableMap<Long ,MutableList<Identifier>> = mutableMapOf()
    private val activeModifiers: MutableMap<Long, AbstractModifier<T>.CompiledModifiers> = mutableMapOf()
    abstract val fallbackData: AbstractModifier<T>.CompiledModifiers

    abstract fun gatherActiveModifiers(stack: ItemStack)

    fun getModifiersById(itemStackId: Long): List<Identifier>{
        return modifiers[itemStackId]?: listOf()
    }

    fun setModifiersById(itemStackId: Long, compiledData: AbstractModifier<T>.CompiledModifiers){
        activeModifiers[itemStackId] = compiledData
    }

    fun addModifier(modifier: Identifier, stack: ItemStack): Boolean{
        val nbt = stack.orCreateNbt
        return addModifier(modifier, stack, nbt)
    }

    protected fun addModifier(modifier: Identifier, stack: ItemStack, nbt: NbtCompound): Boolean{
        val id = Nbt.makeItemStackId(stack)
        if (!modifiers.containsKey(id)) {
            initializeModifiers(nbt, id)
        }
        val highestModifier = checkDescendant(modifier,stack)
        if (highestModifier != null){
            val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
            return if (mod?.hasDescendant() == true){
                val highestDescendantPresent: Int = checkModifierLineage(mod, stack)
                if (highestDescendantPresent < 0){
                    false
                } else {
                    val lineage = mod.getModLineage()
                    val newDescendant = lineage[highestDescendantPresent]
                    val currentGeneration = lineage[max(highestDescendantPresent - 1,0)]
                    modifiers[id]?.add(newDescendant)
                    addModifierToNbt(newDescendant, nbt)
                    removeModifier(stack, currentGeneration, nbt)
                    gatherActiveModifiers(stack)
                    true
                }
            } else {
                false
            }
        }
        addModifierToNbt(modifier, nbt)
        modifiers[id]?.add(modifier)
        gatherActiveModifiers(stack)
        return true

    }

    protected fun checkDescendant(modifier: Identifier, scepter: ItemStack): Identifier?{
        val id = Nbt.getItemStackId(scepter)
        val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
        val lineage = mod?.getModLineage() ?: return modifier
        var highestModifier: Identifier? = null
        lineage.forEach { identifier ->
            if (modifiers[id]?.contains(identifier) == true){
                highestModifier = identifier
            }
        }
        return highestModifier
    }

    protected fun removeModifier(scepter: ItemStack, modifier: Identifier, nbt: NbtCompound){
        val id = Nbt.getItemStackId(nbt)
        modifiers[id]?.remove(modifier)
        gatherActiveModifiers(scepter)
        removeModifierFromNbt(modifier,nbt)
    }

    fun addModifierToNbt(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        ModifierHelper.addModifierToNbt(modifier, nbt)
    }

    fun addModifierToNbt(modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(NbtKeys.MODIFIER_ID.str(),modifier.toString())
        Nbt.addNbtToList(newEl, NbtKeys.MODIFIERS.str(),nbt)
    }

    protected fun removeModifierFromNbt(modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(NbtKeys.MODIFIERS.str(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(NbtKeys.MODIFIER_ID.str())){
                val chk = Identifier(nbtEl.getString(NbtKeys.MODIFIER_ID.str()))
                chk == modifier
            } else {
                false
            }
        }
    }

    open fun initializeModifiers(stack: ItemStack, nbt: NbtCompound){
        if (nbt.contains(NbtKeys.MODIFIERS.str())){
            println("initing")
            val id = Nbt.makeItemStackId(stack)
            initializeModifiers(nbt, id)
            gatherActiveModifiers(stack)
        }
    }

    protected fun initializeModifiers(nbt: NbtCompound, id: Long){
        val nbtList = nbt.getList(NbtKeys.MODIFIERS.str(),10)
        modifiers[id] = mutableListOf()
        for (el in nbtList){
            val compound = el as NbtCompound
            if (compound.contains(NbtKeys.MODIFIER_ID.str())){
                val modifier = compound.getString(NbtKeys.MODIFIER_ID.str())
                modifiers[id]?.add(Identifier(modifier))
            }
        }
    }

    fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.orCreateNbt
        val id = Nbt.makeItemStackId(stack)
        if (!modifiers.containsKey(id)) {
            if (nbt.contains(NbtKeys.MODIFIERS.str())) {
                initializeModifiers(nbt, id)
            }
        }
        return modifiers[id] ?: listOf()
    }

    fun getModifiersFromNbt(stack: ItemStack): List<Identifier>{
        val nbt = stack.orCreateNbt
        val list: MutableList<Identifier> = mutableListOf()
        if (nbt.contains(NbtKeys.MODIFIERS.str())){
            val nbtList = Nbt.readNbtList(nbt, NbtKeys.MODIFIERS.str())
            nbtList.forEach {
                val nbtCompound = it as NbtCompound
                if (nbtCompound.contains(NbtKeys.MODIFIER_ID.str())){
                    list.add(Identifier(Nbt.readStringNbt(NbtKeys.MODIFIER_ID.str(), nbtCompound)))
                }
            }
        }
        return list
    }

    fun getActiveModifiers(stack: ItemStack): AbstractModifier<T>.CompiledModifiers {
        val id = Nbt.getItemStackId(stack)
        if (id != -1L && !activeModifiers.containsKey(id)){
            initializeModifiers(stack, stack.orCreateNbt)
        }
        val compiledData = activeModifiers[id]
        return  compiledData ?: fallbackData
    }

    fun checkModifierLineage(modifier:Identifier, stack: ItemStack): Boolean{
        val mod = ModifierRegistry.getByType<AugmentModifier>(modifier)
        return if (mod != null){
            checkModifierLineage(mod, stack) >= 0
        } else {
            false
        }
    }

    protected fun checkModifierLineage(mod: AugmentModifier, stack: ItemStack): Int{
        val id = Nbt.getItemStackId(stack)
        val lineage = mod.getModLineage()
        val highestOrderDescendant = lineage.size
        var highestDescendantPresent = 0
        lineage.forEachIndexed { index, identifier ->
            if (modifiers[id]?.contains(identifier) == true){
                highestDescendantPresent = index + 1
            }
        }
        return if(highestDescendantPresent < highestOrderDescendant){
            highestDescendantPresent
        } else {
            -1
        }
    }

    inline fun <reified A:AbstractModifier<A>> gatherActiveAbstractModifiers(stack: ItemStack, objectToAffect: Identifier, compiler: AbstractModifier<A>.Compiler): AbstractModifier<A>.CompiledModifiers{
        val id = Nbt.getItemStackId(stack)
        getModifiersById(id).forEach { identifier ->
            val modifier = ModifierRegistry.getByType<A>(identifier)
            if (modifier != null){
                if (!modifier.hasObjectToAffect()){
                    compiler.add(modifier)
                } else {
                    if (modifier.checkObjectsToAffect(objectToAffect)){
                        compiler.add(modifier)
                    }
                }
            }
        }
        return compiler.compile()
    }
}