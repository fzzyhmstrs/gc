package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.coding_util.AugmentDatapoint
import me.fzzyhmstrs.amethyst_core.item_util.AbstractScepterItem
import me.fzzyhmstrs.amethyst_core.misc_util.SyncedConfigHelper.readOrCreate
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

abstract class ScepterAugment(private val tier: Int, private val maxLvl: Int, target: EnchantmentTarget, vararg slot: EquipmentSlot): Enchantment(Rarity.VERY_RARE, target,slot) {
    
    open val baseEffect = AugmentEffect()

    abstract fun applyTasks(world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): Boolean

    fun applyModifiableTasks(world: World, user: LivingEntity, hand: Hand, level: Int, modifiers: List<AugmentModifier> = listOf(), modifierData: CompiledAugmentModifier? = null): Boolean{
        val effectModifiers = AugmentEffect()
        effectModifiers.plus(modifierData?.getEffectModifier()?: AugmentEffect())
        effectModifiers.plus(baseEffect)
        val bl = applyTasks(world,user,hand,level,effectModifiers)
        if (bl) {
            modifiers.forEach {
                if (it.hasSecondaryEffect()) {
                    it.getSecondaryEffect()?.applyModifiableTasks(world, user, hand, level, listOf(), null)
                }
            }
        }
        return bl
    }

    open fun clientTask(world: World, user: LivingEntity, hand: Hand, level: Int){
    }

    open fun entityTask(world: World, target: Entity, user: LivingEntity, level: Double, hit: HitResult?, effects: AugmentEffect){
    }

    open fun soundEvent(): SoundEvent {
        return SoundEvents.BLOCK_BEACON_ACTIVATE
    }

    protected fun toLivingEntityList(list: List<Entity>): List<LivingEntity>{
        val newList: MutableList<LivingEntity> = mutableListOf()
        list.forEach {
            if (it is LivingEntity){
                newList.add(it)
            }
        }
        return newList
    }

    override fun getMinPower(level: Int): Int {
        return 30
    }

    override fun getMaxPower(level: Int): Int {
        return 50
    }

    override fun getMaxLevel(): Int {
        return 1
    }

    open fun getAugmentMaxLevel(): Int{
        return maxLvl
    }

    override fun isTreasure(): Boolean {
        return true
    }

    override fun isAvailableForEnchantedBookOffer(): Boolean {
        return false
    }

    override fun isAvailableForRandomSelection(): Boolean {
        return false
    }


    override fun isAcceptableItem(stack: ItemStack): Boolean {
        acceptableItemStacks().forEach {
            if (stack.isOf(it.item)){
                return true
            }
        }
        return false
    }

    open fun acceptableItemStacks(): MutableList<ItemStack> {
        return mutableListOf()
    }

    fun getTier(): Int{
        return tier
    }

    abstract fun augmentStat(imbueLevel: Int = 1): AugmentDatapoint

    companion object{

        const val augmentVersion = "_v0"

        class AugmentStats {
            var id: String = AbstractScepterItem.defaultId.toString()
            var cooldown: Int = 20
            var manaCost: Int = 2
            var minLvl: Int = 1
        }

        fun configAugment(file: String, configClass: AugmentStats): AugmentStats{
            return readOrCreate(file,"augments") {configClass}
        }
    }
}
