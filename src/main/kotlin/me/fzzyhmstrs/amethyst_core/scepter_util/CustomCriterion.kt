package me.fzzyhmstrs.amethyst_core.scepter_util

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class CustomCriterion(private val id: Identifier): AbstractCriterion<CustomCriterion.CustomConditions>() {

    override fun getId(): Identifier {
        return id
    }

    override fun conditionsFromJson(
        obj: JsonObject,
        playerPredicate: EntityPredicate.Extended,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
    ): CustomConditions {
        return CustomConditions(id,playerPredicate)
    }

    fun trigger(player: ServerPlayerEntity){
        this.trigger(player) { true }
    }

    class CustomConditions(id: Identifier, predicate: EntityPredicate.Extended): AbstractCriterionConditions(id, predicate)
}