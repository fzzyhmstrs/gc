package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object PersistentEffectHelper {

    private val persistentEffects: MutableList<PersistentEffectInstance> = mutableListOf()
    private val DUSTBIN = Dustbin({data -> persistentEffects.remove(data); if (persistentEffects.isEmpty()){persistentEffectsFlag = false}})
    private var persistentEffectsFlag: Boolean = false

    fun persistentEffectTicker(){
        DUSTBIN.clean()
        if (!persistentEffectsFlag) return
        persistentEffects.forEach {
            it.ticker.tickUp()
            if (it.ticker.isReady()){
                val aug = it.augment
                aug.persistentEffect(it.world, it.user, it.blockPos, it.entityList, it.level, it.effect)
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
        persistentEffects.add(PersistentEffectInstance(EventRegistry.Ticker(delay),delay, duration, effect, data))
        persistentEffectsFlag = true
    }

    private data class PersistentEffectInstance(val ticker: EventRegistry.Ticker, val delay: Int, val duration: Int, val augment: PersistentEffect, val data: PersistentEffectData)
    )
    
    class AugmentPersistentEffectData: PersistentEffectData(val world: World, val user: LivingEntity, 
                                                            val blockPos: BlockPos, val entityList: MutableList<Entity>, 
                                                            val level: Int = 1, effect: AugmentEffect)
    
    
    interface AugmentPersistentEffect{
        override fun persistentEffect(data: PersistentEffectData){
            if (data !is AugmentPersistentEffectData) return
            augmentPersistentEffect(
                data.world,
                data.user,
                data.blockPos,
                data.entityList,
                data.level,
                data.effect
            )
        }
        
        fun augmentPersistentEffect(world: World, user: LivingEntity, blockPos: BlockPos, entityList: MutableList<Entity>, level: Int = 1, effect: AugmentEffect)
    }

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(data: PersistentEffectData)
    }
    
    interface PersistentEffectData{
        
    }

}
