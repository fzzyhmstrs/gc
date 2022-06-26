package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import java.util.function.Consumer

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
    //alternative version with the AugmentEffect directly included

object ModifierDefaults{
    val BLANK_ID = Identifier(AC.MOD_ID,"blank_modifier")
    val BLANK_EFFECT = AugmentEffect()
    val BLANK_XP_MOD = XpModifiers()
    val EMPTY_COMPILED = CompiledAugmentModifier(BLANK_ID)
    val BLANK_COMPILED_DATA = CompiledAugmentModifier.CompiledModifiers(listOf(), EMPTY_COMPILED)
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

