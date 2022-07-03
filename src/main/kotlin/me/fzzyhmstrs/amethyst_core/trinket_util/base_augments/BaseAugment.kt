package me.fzzyhmstrs.amethyst_core.trinket_util.base_augments

import me.fzzyhmstrs.amethyst_core.item_util.AcceptableItemStacks
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.*
import java.util.*

/**
 * the base class for Trinket- or other equipment-based augments.
 *
 * See [AbstractAugmentJewelryItem][me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentJewelryItem] for information on building a Trinket Item suitable for use with these augments.
 *
 * [specialEffect]: For use in special circumstances not covered by the usual items. Recommended for use when you need to have a custom code implementation, in a Mixin for example.
 *
 * [equipEffect]: Called by Jewelry Items when equipped to a player.
 *
 * [unequipEffect]: Called by Jewelry Items when unEquipped from a player.
 *
 * [attributeModifier]: Add Minecraft [attribute modifiers][net.minecraft.entity.attribute.EntityAttributeModifier] to apply here.
 */
open class BaseAugment(weight: Rarity, val mxLvl: Int = 1, val target: EnchantmentTarget, vararg slot: EquipmentSlot): Enchantment(weight, target ,slot) {

    open fun specialEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY): Boolean{
        return true
    }

    open fun equipEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

    open fun unequipEffect(user: LivingEntity, level: Int, stack: ItemStack = ItemStack.EMPTY){
        return
    }

    open fun attributeModifier(stack: ItemStack, uuid: UUID): Pair<EntityAttribute,EntityAttributeModifier>? {
        return null
    }

    open fun acceptableItemStacks(): MutableList<ItemStack>{
        return AcceptableItemStacks.baseAcceptableItemStacks(target)
    }

    override fun getMinPower(level: Int): Int {
        return 30
    }

    override fun getMaxPower(level: Int): Int {
        return 50
    }

    override fun getMaxLevel(): Int {
        return mxLvl
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

    companion object{
        private val countQueue: MutableMap<UUID,MutableMap<String,Int>> = mutableMapOf()

        fun addCountToQueue(uuid: UUID,countTag: String,count: Int){
            if (countQueue.containsKey(uuid)){
                countQueue[uuid]?.set(countTag,count)
            } else {
                countQueue[uuid] = mutableMapOf()
                countQueue[uuid]?.set(countTag,count)
            }
        }

        fun readCountFromQueue(uuid: UUID, countTag: String): Int {
            if (countQueue.containsKey(uuid)){
                if (countQueue[uuid]?.containsKey(countTag) == true){
                    return countQueue[uuid]?.getOrDefault(countTag,0)?:0
                }
            }
            return 0
        }
    }
}