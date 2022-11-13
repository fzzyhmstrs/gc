package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import java.util.*

/**
 * Utilities for implementing a persistent effect that isn't linked to a particular object. Once initiated, the object that called it does not necessarily have to continue existing or performing the action that initiated the persistent effect. This is useful in instances where you want an effect to occur over time, but don't want to deal with the headache of coding and managing tracking that persistent effect. All of that is handled here.
 */
object PersistentEffectHelper {

    private val persistentEffects: MutableList<PersistentEffectInstance> = mutableListOf()
    private val persistentEffectQueue: Vector<PersistentEffectInstance> = Vector()
    private val DUSTBIN = Dustbin { instance: PersistentEffectInstance ->
        persistentEffects.remove(instance); if (persistentEffects.isEmpty()) {
        persistentEffectsFlag = false
    }
    }
    private var persistentEffectsFlag: Boolean = false
    private var locked = false

    /**
     * primary function of interest. Basically registers a persistent effect with the built-in ticker for execution. You can pass custom implementations of [PersistentEffectData] and [PersistentEffect] to the parameters as needed.
     *
     * Scepter Augments do this already with the [AugmentPersistentEffect][me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentPersistentEffect] and [AugmentPersistentEffectData][me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentPersistentEffectData] implementations. Successful runtime can be achieved with a simple type check on the persistentEffect call.
     */
    fun setPersistentTickerNeed(
        augment: PersistentEffect,
        delay: Int, duration: Int,
        data: PersistentEffectData
    ){
        if (!locked) {
            persistentEffects.add(PersistentEffectInstance(EventRegistry.Ticker(delay), delay, duration, augment, data))
            persistentEffectsFlag = true
        } else {
            persistentEffectQueue.add(PersistentEffectInstance(EventRegistry.Ticker(delay), delay, duration, augment, data))
        }

    }

    internal fun persistentEffectTicker(){
        locked = true
        DUSTBIN.clean()
        if (!persistentEffectsFlag) {
            locked = false
            if (persistentEffectQueue.isNotEmpty()) {
                persistentEffects.addAll(persistentEffectQueue)
                persistentEffectsFlag = true
                persistentEffectQueue.clear()
            } else {
                return
            }
        }
        for (i in 0 until persistentEffects.size) {
            val it = persistentEffects[i]
            it.ticker.tickUp()
            if (it.ticker.isReady()){
                val aug = it.augment
                aug.persistentEffect(it.data)
                val newDur = it.duration - it.delay
                if (newDur <= 0){
                    DUSTBIN.markDirty(it)
                } else {
                    it.duration = newDur
                }
            }
        }
        locked = false
        if (persistentEffectQueue.isNotEmpty()) {
            persistentEffects.addAll(persistentEffectQueue)
            persistentEffectsFlag = true
            persistentEffectQueue.clear()
        }

    }

    private data class PersistentEffectInstance(val ticker: EventRegistry.Ticker, val delay: Int, var duration: Int, val augment: PersistentEffect, val data: PersistentEffectData)

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(data: PersistentEffectData)
    }
    
    interface PersistentEffectData{
        
    }
}
