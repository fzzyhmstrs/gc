package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.EntityDamageSource

object CustomDamageSources {

    class SoulDamageSource(source: Entity?): EntityDamageSource("soul", source){
    }
    class SmitingDamageSource(source: Entity?): EntityDamageSource("smite", source){
    }

    class LightningDamageSource(source: Entity?): EntityDamageSource("lightningBolt", source){
    }

    class FreezingDamageSource(source: Entity?): EntityDamageSource("freeze", source){
        init{
            this.setBypassesArmor()
        }
    }
}