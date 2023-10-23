package me.fzzyhmstrs.gear_core.modifier_util

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack

object BaseFunctions {

    fun union(firstFunction: EquipmentModifier.DamageFunction, secondFunction: EquipmentModifier.DamageFunction): EquipmentModifier.DamageFunction{
        return EquipmentModifier.DamageFunction { stack, user, attacker, source, amount ->
            val amount1 = firstFunction.test(stack, user, attacker, source, amount)
            secondFunction.test(stack, user, attacker, source, amount1)
        }
    }

    fun union(functions: List<EquipmentModifier.DamageFunction>): EquipmentModifier.DamageFunction{
        return EquipmentModifier.DamageFunction { stack, user, attacker, source, amount ->
            var amount1 = amount
            for (function in functions){
                amount1 = function.test(stack, user, attacker, source, amount1)
            }
            amount1
        }
    }

    open class ChancedAttackFunction(private val chance: Float, private val function: EquipmentModifier.DamageFunction): EquipmentModifier.DamageFunction{
        override fun test(
            stack: ItemStack,
            user: LivingEntity,
            attacker: LivingEntity?,
            source: DamageSource,
            amount: Float
        ): Float {
            if(user.world.random.nextFloat() < chance)
                return function.test(stack, user, attacker, source, amount)
            return amount
        }
    }

    open class RangedAttackFunction(private val multiplier: Float = 1f): EquipmentModifier.DamageFunction{
        override fun test(
            stack: ItemStack,
            user: LivingEntity,
            attacker: LivingEntity?,
            source: DamageSource,
            amount: Float
        ): Float {
            return if(source.source is PersistentProjectileEntity)
                amount * multiplier
            else
                amount
        }
    }

    open class StatusOnAttackFunction(private val effect: StatusEffect, private val duration: Int, private val amplifier: Int): EquipmentModifier.DamageFunction{
        override fun test(
            stack: ItemStack,
            user: LivingEntity,
            attacker: LivingEntity?,
            source: DamageSource,
            amount: Float
        ): Float {
            attacker?.addStatusEffect(StatusEffectInstance(effect,duration, amplifier))
            return amount
        }
    }

    open class DamageMultiplierFunction(private val multiplier: Float): EquipmentModifier.DamageFunction{
        override fun test(
            stack: ItemStack,
            user: LivingEntity,
            attacker: LivingEntity?,
            source: DamageSource,
            amount: Float
        ): Float {
            return amount * (1f + multiplier)
        }
    }

    open class DamageAdderFunction(private val adder: Float): EquipmentModifier.DamageFunction{
        override fun test(
            stack: ItemStack,
            user: LivingEntity,
            attacker: LivingEntity?,
            source: DamageSource,
            amount: Float
        ): Float {
            return amount + adder
        }
    }

}