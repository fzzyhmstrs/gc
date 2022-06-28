package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.registry.EventRegistry

object PersistentEffectHelper {

    private val persistentEffects: MutableList<PersistentEffectInstance> = mutableListOf()
    private val DUSTBIN = Dustbin { instance: PersistentEffectInstance ->
        persistentEffects.remove(instance); if (persistentEffects.isEmpty()) {
        persistentEffectsFlag = false
    }
    }
    private var persistentEffectsFlag: Boolean = false

    fun persistentEffectTicker(){
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
                }
            }
        }
    }

    fun setPersistentTickerNeed(
        augment: PersistentEffect,
        delay: Int, duration: Int,
        data: PersistentEffectData
    ){
        persistentEffects.add(PersistentEffectInstance(EventRegistry.Ticker(delay),delay, duration, augment, data))
        persistentEffectsFlag = true
    }

    private data class PersistentEffectInstance(val ticker: EventRegistry.Ticker, val delay: Int, val duration: Int, val augment: PersistentEffect, val data: PersistentEffectData)

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(data: PersistentEffectData)
    }
    
    interface PersistentEffectData{
        
    }
}
