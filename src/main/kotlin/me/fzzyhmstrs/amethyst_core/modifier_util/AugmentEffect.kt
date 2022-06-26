package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlD
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlF
import me.fzzyhmstrs.amethyst_core.coding_util.PerLvlI
import net.minecraft.entity.LivingEntity
import java.util.function.Consumer
import kotlin.math.max

data class AugmentEffect(
    private var damageData: PerLvlF = PerLvlF(),
    private var amplifierData: PerLvlI = PerLvlI(),
    private var durationData: PerLvlI = PerLvlI(),
    private var rangeData: PerLvlD = PerLvlD()
){
    private var goodConsumers: MutableList<AugmentConsumer> = mutableListOf()
    private var badConsumers: MutableList<AugmentConsumer> = mutableListOf()

    fun plus(ae: AugmentEffect){
        damageData = damageData.plus(ae.damageData)
        amplifierData = amplifierData.plus(ae.amplifierData)
        durationData = durationData.plus(ae.durationData)
        rangeData = rangeData.plus(ae.rangeData)
        goodConsumers.addAll(ae.goodConsumers)
        badConsumers.addAll(ae.badConsumers)
    }
    fun damage(level: Int = 0): Float{
        return max(0.0F, damageData.value(level))
    }
    fun amplifier(level: Int = 0): Int{
        return max(0, amplifierData.value(level))
    }
    fun duration(level: Int = 0): Int{
        return max(0, durationData.value(level))
    }
    fun range(level: Int = 0): Double{
        return max(1.0, rangeData.value(level))
    }
    fun consumers(): MutableList<AugmentConsumer>{
        val list = mutableListOf<AugmentConsumer>()
        list.addAll(goodConsumers)
        list.addAll(badConsumers)
        return list
    }
    fun accept(list: List<LivingEntity>, type: AugmentConsumer.Type? = null){
        when (type){
            AugmentConsumer.Type.BENEFICIAL ->{
                goodConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
            AugmentConsumer.Type.HARMFUL ->{
                badConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
            else->{
                goodConsumers.forEach {
                    it.consumer.accept(list)
                }
                badConsumers.forEach {
                    it.consumer.accept(list)
                }
            }
        }
    }
    fun accept(entity: LivingEntity, type: AugmentConsumer.Type? = null){
        accept(listOf(entity), type)
    }

    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentEffect {
        return this.copy(damageData = PerLvlF(damage, damagePerLevel, damagePercent))
    }
    fun addDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData.plus(PerLvlF(damage, damagePerLevel, damagePercent))
    }
    fun addDamage(ae: AugmentEffect){
        damageData.plus(ae.damageData)
    }
    fun setDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData = PerLvlF(damage, damagePerLevel, damagePercent)
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect {
        return this.copy(amplifierData = PerLvlI(amplifier, amplifierPerLevel, amplifierPercent))
    }
    fun addAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData.plus(PerLvlI(amplifier, amplifierPerLevel, amplifierPercent))
    }
    fun addAmplifier(ae: AugmentEffect){
        amplifierData.plus(ae.amplifierData)
    }
    fun setAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData = PerLvlI(amplifier, amplifierPerLevel, amplifierPercent)
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect {
        return this.copy(durationData = PerLvlI(duration, durationPerLevel, durationPercent))
    }
    fun addDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData.plus(PerLvlI(duration, durationPerLevel, durationPercent))
    }
    fun addDuration(ae: AugmentEffect){
        durationData.plus(ae.durationData)
    }
    fun setDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData = PerLvlI(duration, durationPerLevel, durationPercent)
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect {
        return this.copy(rangeData = PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData.plus(PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(ae: AugmentEffect){
        rangeData.plus(ae.rangeData)
    }
    fun setRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData = PerLvlD(range, rangePerLevel, rangePercent)
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentEffect {
        addConsumer(consumer, type)
        return this
    }
    fun addConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type){
        if (type == AugmentConsumer.Type.BENEFICIAL){
            goodConsumers.add(AugmentConsumer(consumer, type))
        } else {
            badConsumers.add(AugmentConsumer(consumer, type))
        }
    }
    fun addConsumers(list: List<AugmentConsumer>){
        list.forEach {
            if (it.type == AugmentConsumer.Type.BENEFICIAL){
                goodConsumers.add(AugmentConsumer(it.consumer, it.type))
            } else {
                badConsumers.add(AugmentConsumer(it.consumer, it.type))
            }
        }
    }
    fun setConsumers(list: MutableList<AugmentConsumer>, type: AugmentConsumer.Type){
        if (type == AugmentConsumer.Type.BENEFICIAL){
            goodConsumers = list
        } else {
            badConsumers = list
        }
    }
    fun setConsumers(ae: AugmentEffect){
        goodConsumers = ae.goodConsumers
        badConsumers = ae.badConsumers
    }
}