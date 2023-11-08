package me.fzzyhmstrs.gear_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import io.netty.util.ResourceLeakDetector.isEnabled
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ConfigEquipmentModifier(
    modifierId: Identifier = AbstractModifierHelper.BLANK,
    target: EquipmentModifierTarget = EquipmentModifierTarget.NONE,
    weight: Int = 10,
    rarity: Rarity = Rarity.COMMON,
    persistent: Boolean = false,
    availableForSelection: Boolean = true)
: EquipmentModifier(
    modifierId,
    target,
    weight,
    rarity,
    persistent,
    availableForSelection)
{

    private val blank = ArrayListMultimap.create<EntityAttribute, EntityAttributeModifierContainer>()

    override fun attributeModifiers(): Multimap<EntityAttribute, EntityAttributeModifierContainer> {
        if (!isEnabled()) return blank
        return super.attributeModifiers()
    }

    override fun modifiers(): List<Identifier> {
        if (!isEnabled()) return listOf()
        return super.modifiers()
    }

    override fun modifyDurability(durability: Int): Int {
        if (!isEnabled()) return durability
        return super.modifyDurability(durability)
    }

    override fun postHit(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (!isEnabled()) return
        super.postHit(stack, user, target)
    }

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity) {
        if (!isEnabled()) return
        super.postMine(stack, world, state, pos, miner)
    }

    override fun onUse(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (!isEnabled()) return
        super.onUse(stack, user, target)
    }

    override fun onAttack(
        stack: ItemStack,
        user: LivingEntity,
        attacker: LivingEntity?,
        source: DamageSource,
        amount: Float
    ): Float {
        if (!isEnabled()) return amount
        return super.onAttack(stack, user, attacker, source, amount)
    }

    override fun onDamaged(
        stack: ItemStack,
        user: LivingEntity,
        attacker: LivingEntity?,
        source: DamageSource,
        amount: Float
    ): Float {
        if (!isEnabled()) return amount
        return super.onDamaged(stack, user, attacker, source, amount)
    }

    override fun killedOther(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (!isEnabled()) return
        super.killedOther(stack, user, target)
    }

    override fun tick(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (!isEnabled()) return
        super.tick(stack, user, target)
    }

    override fun randomlySelectable(): Boolean{
        return isEnabled() && super.randomlySelectable()
    }
}
