package me.fzzyhmstrs.gear_core.modifier_util.serialization

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.gear_core.GC
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface DamageTransformerType<T: DamageTransformer> {

    fun codec(): Codec<T>

    companion object{
        val REGISTRY = FzzyPort.simpleRegistry<DamageTransformerType<*>>(Identifier(GC.MOD_ID,"damage_transformer_type"))
        val CODEC: Codec<DamageTransformer> = REGISTRY.codec.dispatch({ p: DamageTransformer -> p.getType()},{ t -> t.codec()})
        val LIST_CODEC: Codec<List<DamageTransformer>> = Codec.either(CODEC, CODEC.listOf()).xmap(
            {e -> e.map({l -> listOf(l)},{r -> r})},
            {l -> if (l.size == 1) Either.left(l[0]) else Either.right(l)}
        )

        fun <T: DamageTransformer> register(type: DamageTransformerType<T>, identifier: Identifier): DamageTransformerType<T>{
            return Registry.register(REGISTRY,identifier,type)
        }
    }

    object Types{
        fun init(){}
    }

}