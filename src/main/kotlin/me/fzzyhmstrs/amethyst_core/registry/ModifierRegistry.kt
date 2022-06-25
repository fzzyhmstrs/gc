package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.scepter_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.scepter_util.AugmentModifierDefaults
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

object ModifierRegistry {
    private val registry: MutableMap<Identifier, AugmentModifier> = mutableMapOf()
    fun register(modifier: AugmentModifier){
        val id = modifier.modifierId
        if (registry.containsKey(id)){throw IllegalStateException("Modifier with id $id already present in ModififerRegistry")}
        registry[id] = modifier
    }
    fun get(id: Identifier): AugmentModifier?{
        return registry[id]
    }
    fun getByRawId(rawId: Int): AugmentModifier?{
        return registry[registry.keys.elementAtOrElse(rawId) { AugmentModifierDefaults.blankId }]
    }
    fun getIdByRawId(rawId:Int): Identifier {
        return registry.keys.elementAtOrElse(rawId) { AugmentModifierDefaults.blankId }
    }
    fun getRawId(id: Identifier): Int{
        return registry.keys.indexOf(id)
    }
    fun getName(id: Identifier): Text {
        return TranslatableText("scepter.modifiers.$id")
    }
    fun isModifier(id: Identifier): Boolean{
        return this.get(id) != null
    }
}