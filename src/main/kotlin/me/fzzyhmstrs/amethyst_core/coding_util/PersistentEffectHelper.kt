package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object PersistentEffectHelper {

    private val persistentEffects: MutableList<PersistentEffectData> = mutableListOf()
    private val persistentEffectsMarkedForRemoval: MutableList<PersistentEffectData> = mutableListOf()
    private var persistentEffectsFlag: Boolean = false
    private var markedForRemovalFlag: Boolean = false

    fun persistentEffectTicker(){
        if (markedForRemovalFlag){
            persistentEffects.removeAll(persistentEffectsMarkedForRemoval)
            if (persistentEffects.isEmpty()) {
                persistentEffectsFlag = false
            }
            persistentEffectsMarkedForRemoval.clear()
            markedForRemovalFlag = false
        }
        if (!persistentEffectsFlag) return
        persistentEffects.forEach {
            it.ticker.tickUp()
            if (it.ticker.isReady()){
                val aug = it.augment
                val world = it.world
                val user = it.user
                val blockPos = it.blockPos
                val entityList = it.entityList
                val level = it.level
                val effect = it.effect
                aug.persistentEffect(world, user, blockPos, entityList, level, effect)
                val dur = it.duration
                val newDur = dur - it.delay
                if (newDur <= 0){
                    persistentEffectsMarkedForRemoval.add(it)
                    markedForRemovalFlag = true
                }
            }
        }
    }

    fun setPersistentTickerNeed(
        world: World, user: LivingEntity,
        entityList: MutableList<Entity>,
        level: Int, blockPos: BlockPos,
        augment: PersistentEffect,
        delay: Int, duration: Int,
        effect: AugmentEffect
    ){
        persistentEffects.add(PersistentEffectData(world,user,entityList,level,blockPos,augment,delay,duration, effect, EventRegistry.Ticker(delay)))
        persistentEffectsFlag = true
    }

    private data class PersistentEffectData(val world: World, val user: LivingEntity,
                                            val entityList: MutableList<Entity>, val level: Int, val blockPos: BlockPos,
                                            val augment: PersistentEffect, var delay: Int, var duration: Int,
                                            val effect: AugmentEffect, val ticker: EventRegistry.Ticker
    )

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(world: World, user: LivingEntity, blockPos: BlockPos, entityList: MutableList<Entity>, level: Int = 1, effect: AugmentEffect)
    }

}
