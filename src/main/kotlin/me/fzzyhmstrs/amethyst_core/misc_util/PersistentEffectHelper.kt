package me.fzzyhmstrs.amethyst_core.misc_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.PersistentEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object PersistentEffectHelper {

    private val persistentEffect: MutableMap<Int, PersistentEffectData> = mutableMapOf()
    private val persistentEffectNeed: MutableMap<Int,Int> = mutableMapOf()

    @Deprecated("moving to amethyst_core")
    fun persistentEffectTicker(id: Int){
        val curTick = persistentEffectNeed[id]?:return
        val delay = persistentEffect[id]?.delay?:return
        if (curTick >= delay){
            val aug = persistentEffect[id]?.augment?:return
            val world = persistentEffect[id]?.world?:return
            val user = persistentEffect[id]?.user?:return
            val blockPos = persistentEffect[id]?.blockPos?:return
            val entityList = persistentEffect[id]?.entityList?:return
            val level = persistentEffect[id]?.level?:return
            val effect = persistentEffect[id]?.effect?:return
            aug.persistentEffect(world, user,blockPos, entityList, level, effect)
            val dur = persistentEffect[id]?.duration
            if(dur != null){
                val newDur = dur - delay
                persistentEffect[id]?.duration = newDur
                if (newDur <= 0) {
                    persistentEffectNeed[id] = -1
                    return
                } else {
                    persistentEffectNeed[id] = 0
                }
            }

        } else {
            persistentEffectNeed[id] = curTick + 1
            return
        }
    }

    @Deprecated("moving to amethyst_core")
    fun getPersistentTickerNeed(id:Int): Boolean{
        val chk = persistentEffectNeed[id]?:-1
        return (chk >= 0)
    }
    @Deprecated("moving to amethyst_core")
    fun setPersistentTickerNeed(
        id: Int, world: World, user: LivingEntity,
        entityList: MutableList<Entity>,
        level: Int, blockPos: BlockPos,
        augment: PersistentEffect,
        delay: Int, duration: Int,
        effect: AugmentEffect
    ){
        persistentEffect[id] = PersistentEffectData(world,user,entityList,level,blockPos,augment,delay,duration, effect)
        persistentEffectNeed[id] = 0
    }

    private data class PersistentEffectData(val world: World, val user: LivingEntity,
                                            val entityList: MutableList<Entity>, val level: Int, val blockPos: BlockPos,
                                            val augment: PersistentEffect, var delay: Int, var duration: Int, val effect: AugmentEffect
    )

}