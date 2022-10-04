package me.fzzyhmstrs.amethyst_core.trinket_util

import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.trinket_util.base_augments.AbstractActiveAugment
import me.fzzyhmstrs.amethyst_core.trinket_util.base_augments.AbstractPassiveAugment
import me.fzzyhmstrs.amethyst_core.trinket_util.base_augments.AbstractUsedActiveAugment
import me.fzzyhmstrs.amethyst_core.trinket_util.base_augments.BaseAugment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * interface for use with an item that wants to interact with [Equipment Augments][BaseAugment]. Some methods below interact with any extension of the BaseAugment itself, some are more specialized.
 *
 * Provides a series of helper methods for accessing the effects of equipment augments without any custom boilerplate.
 */
interface AugmentTasks {

    //not covered by this interface are "specialEffect" methods. Those need to be called specifically from

    /**
     * used with an [AbstractActiveAugment] for accessing the activateEffect method
     */
    fun activeEnchantmentTasks(stack: ItemStack, world: World, entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants.keys){
            if (enchant is AbstractActiveAugment){
                val lvl = enchants[enchant] ?: 1
                enchant.baseActivateEffect(entity,lvl, stack)
            }
        }
    }

    /**
     * used with an [AbstractActiveAugment] for deactivating any currently active effects via the deactivateEffect method
     */
    fun inactiveEnchantmentTasks(stack: ItemStack,world: World, entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants.keys){
            if (enchant is AbstractActiveAugment){
                val lvl = enchants[enchant] ?: 1
                enchant.baseDeactivateEffect(entity,lvl,stack)
            }
        }
    }

    /**
     * used with an [AbstractUsedActiveAugment] for accessing the useEffect method
     */
    fun usageEnchantmentTasks(stack: ItemStack,world: World,entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants.keys){
            if (enchant is AbstractUsedActiveAugment){
                val lvl = enchants[enchant] ?: 1
                enchant.baseUseEffect(entity,lvl,stack)
            }
        }
    }

    /**
     * used with [AbstractPassiveAugment] for accessing the tickEffect method
     */
    fun passiveEnchantmentTasks(stack: ItemStack,world: World,entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants.keys){
            if (enchant is AbstractPassiveAugment){
                val lvl = enchants[enchant] ?: 1
                enchant.baseTickEffect(entity,lvl,stack)
            }
        }
    }

    /**
     * used with [BaseAugment] for accessing the equipEffect method
     */
    fun equipEnchantmentTasks(stack: ItemStack,world: World,entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants){
            val aug = enchant.key
            if (aug is BaseAugment){
                val lvl = enchant.value
                aug.baseEquipEffect(entity,lvl,stack)
            }
        }
    }

    /**
     * used with [BaseAugment] for accessing the unequipEffect method
     */
    fun unequipEnchantmentTasks(stack: ItemStack,world: World,entity: Entity){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants){
            val aug = enchant.key
            if (aug is BaseAugment){
                val lvl = enchant.value
                aug.baseUnequipEffect(entity,lvl,stack)
            }
        }
    }

    /**
     * used with [BaseAugment] for accessing the attributeModifier method
     */
    fun modifierEnchantmentTasks(stack: ItemStack,world: World,entity: Entity, map: Multimap<EntityAttribute, EntityAttributeModifier>){
        if (entity !is LivingEntity) return
        val enchants = EnchantmentHelper.get(stack)
        for (enchant in enchants.keys){
            if (enchant is BaseAugment){
                val modifier: Pair<EntityAttribute, EntityAttributeModifier> = enchant.baseAttributeModifier(stack,entity.uuid)?:continue
                map.put(modifier.first,modifier.second)
            }
        }
    }

}