package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.PlayerParticles.scepterParticlePos
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.ParticleEmitting
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.raycaster_util.RaycasterUtil
import me.fzzyhmstrs.amethyst_core.scepter_util.base_augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper.fallbackId
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.*
import net.minecraft.util.hit.HitResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.max

@Suppress("SameParameterValue", "unused", "USELESS_IS_CHECK")
abstract class DefaultScepterItem(material: ScepterToolMaterial, settings: Settings):
    AugmentScepterItem(material,settings), ParticleEmitting{

    init{
        ParticleEmitting.registerParticleEmitter("scepter_smoke_emitter") { client -> doSmoke(client) }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
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

    companion object{
        val commaText: MutableText = LiteralText(", ").formatted(Formatting.GOLD)

        private fun doSmoke(client: MinecraftClient){
            val world = client.world
            val entity = client.player
            if (world != null && entity != null){
                doSmoke(world,client,entity)
            }
        }

        private fun doSmoke(world: World, client: MinecraftClient, user: LivingEntity){
            val particlePos = scepterParticlePos(client, user)
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,particlePos.x,particlePos.y,particlePos.z,user.velocity.x,user.velocity.y + 0.5,user.velocity.z)
        }

        fun addDefaultEnchantment(stack: ItemStack){
            val item = stack.item
            if (item is AbstractScepterItem) {
                val enchantToAdd = Registry.ENCHANTMENT.get(item.fallbackId)
                if (enchantToAdd != null){
                    if (EnchantmentHelper.getLevel(enchantToAdd,stack) == 0){
                        stack.addEnchantment(enchantToAdd,1)
                    }
                }
            }
        }
    }
}
