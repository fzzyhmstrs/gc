package me.fzzyhmstrs.amethyst_core.trinket_util

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance

/**
 * A queue for consistently applying multiple status effects to entities in a centralized way.
 *
 * Avoid issues with objects that are constantly reapplying status effects that causes the status effect list to bounce around or change order distractingly.
 *
 * Effects in this queue are applied on a tick signal, not immediately. The maximum delay to application is one tick.
 */
object EffectQueue {

    private val effectQueue: MutableMap<LivingEntity,MutableMap<StatusEffect,MutableList<Pair<Int,Int>>>> = mutableMapOf()
    private var checkEffects: Boolean = false

    /**
     * call this to apply a status effect to the specified [livingEntity]
     */
    fun addStatusToQueue(livingEntity: LivingEntity, effect: StatusEffect, duration: Int, amplifier: Int){
        if (effectQueue.containsKey(livingEntity)) {
            if (effectQueue[livingEntity]?.containsKey(effect) != true) {
                effectQueue[livingEntity]?.set(effect, mutableListOf())
            }
            effectQueue[livingEntity]?.get(effect)?.add(Pair(duration,amplifier))
        } else {
            effectQueue[livingEntity] = mutableMapOf(effect to mutableListOf(Pair(duration,amplifier)))
        }
        checkEffects = true
    }


    internal fun applyEffects(){
        for ((entity,statusMap) in effectQueue){
            if (entity.isDead|| entity.isRemoved) continue
            for ((effect,effectList) in statusMap){
                for ((dur,amp) in effectList) {
                    entity.addStatusEffect(StatusEffectInstance(effect, dur, amp))
                }
            }
        }
        //clear out the queue if any are needed
        clearStatusesFromQueue()
    }

    private fun clearStatusesFromQueue(){
        effectQueue.clear()
        checkEffects = false
    }

    internal fun checkEffectsQueue(): Boolean{
        return checkEffects
    }

}