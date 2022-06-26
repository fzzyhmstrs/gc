package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.misc_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.raycaster_util.RaycasterUtil
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.hit.HitResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.max

@Suppress("SameParameterValue", "unused", "USELESS_IS_CHECK")
abstract class AbstractAiScepterItem(material: ToolMaterial, settings: Settings, baseRegen: Int, vararg defaultModifier: Identifier): AbstractScepterItem(material,settings, baseRegen, *defaultModifier){
    
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val nbt = stack.orCreateNbt
        val activeSpell = if (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) {
            val activeEnchantId = Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt)
            TranslatableText("enchantment.amethyst_imbuement.${Identifier(activeEnchantId).path}")
        } else {
            TranslatableText("enchantment.amethyst_imbuement.none")
        }
        tooltip.add(TranslatableText("scepter.active_spell").formatted(Formatting.GOLD).append(activeSpell.formatted(Formatting.GOLD)))
        val stats = ScepterHelper.getScepterStats(stack)
        val furyText = TranslatableText("scepter.fury.lvl").string + stats[0].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[3],stats[0]).toString()
        tooltip.add(LiteralText(furyText).formatted(SpellType.FURY.fmt()))
        val graceText = TranslatableText("scepter.grace.lvl").string + stats[1].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[4],stats[1]).toString()
        tooltip.add(LiteralText(graceText).formatted(SpellType.GRACE.fmt()))
        val witText = TranslatableText("scepter.wit.lvl").string + stats[2].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[5],stats[2]).toString()
        tooltip.add(LiteralText(witText).formatted(SpellType.WIT.fmt()))
        val modifierList = ScepterHelper.getModifiers(stack)
        if (modifierList.isNotEmpty()){
            val modifierText = TranslatableText("scepter.modifiers").formatted(Formatting.GOLD)

            val itr = modifierList.asIterable().iterator()
            while(itr.hasNext()){
                val mod = itr.next()
                modifierText.append(TranslatableText("scepter.modifiers.${mod}").formatted(Formatting.GOLD))
                if (itr.hasNext()){
                    modifierText.append(commaText)
                }
            }
            tooltip.add(modifierText)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val activeEnchantId: String
        val testEnchant: Any
        var nbt = stack.nbt
        if (nbt != null) {
            if  (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) {
                activeEnchantId = ScepterHelper.activeEnchantHelper(world,stack,Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt))
                testEnchant = Registry.ENCHANTMENT.get(Identifier(activeEnchantId))?: return resetCooldown(stack,world,user,activeEnchantId)
            } else {
                ScepterHelper.initializeScepter(stack,world)
                activeEnchantId = ScepterHelper.activeEnchantHelper(world,stack,Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt))
                testEnchant = Registry.ENCHANTMENT.get(Identifier(activeEnchantId))?: return resetCooldown(stack,world,user,activeEnchantId)
            }
        } else {
            ScepterHelper.initializeScepter(stack,world)
            nbt = stack.nbt
            if (nbt != null) {
                activeEnchantId =
                    ScepterHelper.activeEnchantHelper(world,stack, Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt))
                testEnchant =
                    Registry.ENCHANTMENT.get(Identifier(activeEnchantId)) ?: return resetCooldown(
                        stack,
                        world,
                        user,
                        activeEnchantId
                    )
            } else {
                return TypedActionResult.fail(stack)
            }
        }
        if (testEnchant !is ScepterAugment) return resetCooldown(stack,world,user,activeEnchantId)


        //determine the level at which to apply the active augment, from 1 to the maximum level the augment can operate
        val level = ScepterHelper.getScepterStat(nbt,activeEnchantId).first
        val minLvl = ScepterHelper.getAugmentMinLvl(activeEnchantId)
        val maxLevel = (testEnchant.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (level >= minLvl){
            testLevel = level
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }

        val stack2 = if (hand == Hand.MAIN_HAND) {
            user.offHandStack
        } else {
            user.mainHandStack
        }
        if(world.isClient()) {
            if (!stack2.isEmpty) {
                if (stack2.item is BlockItem) {
                    val cht = MinecraftClient.getInstance().crosshairTarget
                    if (cht != null) {
                        if (cht.type == HitResult.Type.BLOCK) {
                            return TypedActionResult.pass(stack)
                        }
                    }
                }
            }
            return clientUse(world, user, hand, stack, activeEnchantId, testEnchant,testLevel)
        } else {
            if (!stack2.isEmpty) {
                if (stack2.item is BlockItem) {
                    val reachDistance = if (user.abilities.creativeMode){
                        5.0
                    } else {
                        4.5
                    }
                    val cht = RaycasterUtil.raycastBlock(distance = reachDistance,entity = user)
                    if (cht != null) {
                        return TypedActionResult.pass(stack)
                    }
                }
            }
            return serverUse(world, user, hand, stack, activeEnchantId, testEnchant, testLevel)
        }
    }

    private fun serverUse(world: World, user: PlayerEntity, hand: Hand, stack: ItemStack,
                          activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int): TypedActionResult<ItemStack>{

        val modifiers = ScepterHelper.getActiveModifiers(stack)

        val cd : Int? = ScepterHelper.useScepter(activeEnchantId, stack, user, world, modifiers.compiledData.cooldownModifier)
        return if (cd != null) {
            val manaCost = ScepterHelper.getAugmentManaCost(activeEnchantId,modifiers.compiledData.manaCostModifier)
            if (!ScepterHelper.checkManaCost(manaCost,stack,world,user)) return resetCooldown(stack,world,user,activeEnchantId)
            val level = max(1,testLevel + modifiers.compiledData.levelModifier)
            if (testEnchant.applyModifiableTasks(world, user, hand, level, modifiers.modifiers, modifiers.compiledData)) {
                ScepterHelper.applyManaCost(manaCost,stack, world, user)
                ScepterHelper.incrementScepterStats(stack.orCreateNbt, activeEnchantId, modifiers.compiledData.getXpModifiers())
                user.itemCooldownManager.set(stack.item, cd)
                TypedActionResult.success(stack)
            } else {
                resetCooldown(stack,world,user,activeEnchantId)
            }
        } else {
            resetCooldown(stack,world,user,activeEnchantId)
        }
    }
    @Suppress("UNUSED_PARAMETER")
    private fun clientUse(world: World, user: PlayerEntity, hand: Hand, stack: ItemStack,
                          activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int): TypedActionResult<ItemStack>{
        testEnchant.clientTask(world,user,hand,testLevel)
        return TypedActionResult.pass(stack)
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        addDefaultEnchantment(stack)
        writeDefaultNbt(stack)
        ScepterHelper.initializeScepter(stack,world)
    }

    //removes cooldown on the item if you switch item
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) return
        if (entity !is PlayerEntity) return

        val id = ScepterHelper.scepterTickNbtCheck(stack)
        if (id > 0){
            if (PersistentEffectHelper.getPersistentTickerNeed(id)){
                PersistentEffectHelper.persistentEffectTicker(id)
            }
            val chk = ScepterHelper.shouldRemoveCooldownChecker(id)
            if (chk > 0){
                entity.itemCooldownManager.set(stack.item,chk)
                ScepterHelper.activeEnchantHelper(world,stack, fallbackId.toString())
            } else if (chk == 0){
                entity.itemCooldownManager.remove(stack.item)
            }
            //slowly heal damage over time
            if (ScepterHelper.tickTicker(id)){
                healDamage(1,stack)
            }
        }

    }

    private fun resetCooldown(stack: ItemStack, world: World, user: PlayerEntity, activeEnchant: String): TypedActionResult<ItemStack>{
        world.playSound(null,user.blockPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,0.6F,0.8F)
        ScepterHelper.resetCooldown(stack,activeEnchant)
        if (user is ServerPlayerEntity) {
            sendSmokePacket(user)
        } else {
            doSmoke(world,user)
        }
        return TypedActionResult.fail(stack)
    }

    //companion object for the scepter item, handles private functions and other housekeeping
}
