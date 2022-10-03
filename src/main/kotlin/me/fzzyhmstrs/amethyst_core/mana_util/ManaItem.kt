package me.fzzyhmstrs.amethyst_core.mana_util

import me.fzzyhmstrs.amethyst_core.coding_util.AcText
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

/**
 * An item that extends ManaItem can be damaged and repaired in an item-safe way. Using the heal and damage methods here won't break an item.
 *
 * Additional functionality for checking if a Mana Item has enough mana to do a specified action.
 */
interface ManaItem {

    fun getRepairTime(): Int

    fun healDamage(amount: Int, stack: ItemStack): Int{
        val healedAmount = min(amount,stack.damage)
        stack.damage = max(0,stack.damage - amount)
        return healedAmount
    }

    fun checkCanUse(
        stack: ItemStack,
        world: World,
        entity: PlayerEntity,
        amount: Int,
        message: Text = AcText.translatable("augment_damage.check_can_use")
    ): Boolean {
        val damage = stack.damage
        val maxDamage = stack.maxDamage
        val damageLeft = maxDamage - damage
        return if (damageLeft >= amount) {
            true
        } else {
            if (message.asString() != "") {
                world.playSound(
                    null,
                    entity.blockPos,
                    SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
                )
                entity.sendMessage(message,false)
            }
            false
        }
    }

    /**
     * a method for "burning out" enchantments on a ManaItem. A burned out enchantment will be removed from the item. Useful for something like a timed enchantment, or perhaps an augment with limited uses.
     */
    fun burnOutHandler(
        stack: ItemStack,
        aug: Enchantment,
        entity: PlayerEntity,
        message: Text = AcText.translatable("augment_damage.burnout").append(aug.getName(1))) {
        val enchantList = EnchantmentHelper.get(stack)
        val newEnchantList: MutableMap<Enchantment, Int> = mutableMapOf()
        for (enchant in enchantList.keys) {
            if (enchant != aug) {
                newEnchantList[enchant] = enchantList[enchant] ?: 0
            }
        }
        if (message.asString() != "") {
            entity.sendMessage(message,false)
        }
        EnchantmentHelper.set(newEnchantList, stack)
    }

    /**
     * calling with the [unbreakingFlag] set to true will apply unbreaking to mana damage. By default a mana item is not affected by unbreaking (parameter defaults to false)
     */
    fun manaDamage(
        stack: ItemStack,
        world: World,
        entity: PlayerEntity,
        amount: Int,
        message: Text = AcText.translatable("augment_damage.damage"),
        unbreakingFlag: Boolean = false): Boolean {
        val currentDmg = stack.damage
        val maxDmg = stack.maxDamage
        var newCurrentDmg = currentDmg
        if (currentDmg == (maxDmg - 1)) return true
        for (i in 1..amount) {
            newCurrentDmg++
            val percentDmg = (newCurrentDmg.toDouble() / maxDmg.toDouble() * 100.0).toInt()
            if (percentDmg == 25 || percentDmg == 50) {
                world.playSound(
                    null,
                    entity.blockPos,
                    SoundEvents.BLOCK_GLASS_BREAK,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
                )
            } else if (percentDmg == 75) {
                world.playSound(
                    null,
                    entity.blockPos,
                    SoundEvents.BLOCK_GLASS_BREAK,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
                )
                if (message.asString() != "") {
                    entity.sendMessage(message,false)
                }
            }
            if (newCurrentDmg == (maxDmg - 1)) {
                if (!unbreakingFlag) {
                    stack.damage = newCurrentDmg
                } else {
                    unbreakingDamage(stack,entity,newCurrentDmg - currentDmg)
                }
                if (message.asString() != "") {
                    world.playSound(
                        null,
                        entity.blockPos,
                        SoundEvents.ITEM_SHIELD_BREAK,
                        SoundCategory.NEUTRAL,
                        0.6F,
                        1.2F
                    )
                    world.playSound(
                        null,
                        entity.blockPos,
                        SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        SoundCategory.NEUTRAL,
                        1.0F,
                        1.0F
                    )
                }
                return true
            }
        }
        if (!unbreakingFlag) {
            stack.damage = newCurrentDmg
        } else {
            unbreakingDamage(stack,entity,newCurrentDmg - currentDmg)
        }
        return false
    }

    private fun unbreakingDamage(stack: ItemStack,entity: PlayerEntity, amount: Int){
        for (i in 1..amount){
            stack.damage(1,entity.random, null)
        }
    }
}
