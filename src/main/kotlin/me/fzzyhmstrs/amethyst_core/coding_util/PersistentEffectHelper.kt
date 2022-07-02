package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.registry.EventRegistry

/**
 * Utilities for implementing a persistent effect that isn't linked to a particular object. Once initiated, the object that called it does not necessarily have to continue existing or performing the action that initiated the persistent effect. This is useful in instances where you want an effect to occur over time, but don't want to deal with the headache of coding and managing tracking that persistent effect. All of that is handled here.
 */
object PersistentEffectHelper {

    private val persistentEffects: MutableList<PersistentEffectInstance> = mutableListOf()
    private val DUSTBIN = Dustbin { instance: PersistentEffectInstance ->
        persistentEffects.remove(instance); if (persistentEffects.isEmpty()) {
        persistentEffectsFlag = false
    }
    }
    private var persistentEffectsFlag: Boolean = false

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
        persistentEffects.add(PersistentEffectInstance(EventRegistry.Ticker(delay),delay, duration, augment, data))
        persistentEffectsFlag = true
    }

    internal fun persistentEffectTicker(){
        DUSTBIN.clean()
        if (!persistentEffectsFlag) return
        persistentEffects.forEach {
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
    }

    private data class PersistentEffectInstance(val ticker: EventRegistry.Ticker, val delay: Int, var duration: Int, val augment: PersistentEffect, val data: PersistentEffectData)

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(data: PersistentEffectData)
    }
    
    interface PersistentEffectData{
        
    }
}
