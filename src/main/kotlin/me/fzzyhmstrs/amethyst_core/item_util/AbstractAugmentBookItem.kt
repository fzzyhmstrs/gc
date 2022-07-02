package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class AbstractAugmentBookItem(settings: Settings) : CustomFlavorItem(settings) {

    abstract val loreTier: LoreTier

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val nbt = stack.orCreateNbt
        if (nbt.contains(NbtKeys.LORE_KEY.str())){
            val bola = Identifier(Nbt.readStringNbt(NbtKeys.LORE_KEY.str(),nbt)).toString()
            tooltip.add(TranslatableText("lore_book.augment").formatted(Formatting.GOLD).append(TranslatableText("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}").formatted(Formatting.GOLD)))
            tooltip.add(TranslatableText("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}.desc").formatted(Formatting.WHITE))
            val type = AugmentHelper.getAugmentType(bola)
            if (type == SpellType.NULL){
                tooltip.add(TranslatableText("lore_book.${type.str()}").formatted(type.fmt()))
            } else {
                val lvl = AugmentHelper.getAugmentMinLvl(bola)
                tooltip.add(TranslatableText("lore_book.${type.str()}").formatted(type.fmt()).append(LiteralText(lvl.toString())))
            }
            val item = AugmentHelper.getAugmentItem(bola)
            if (item != Items.AIR) {
                val itemText = item.name.shallowCopy().formatted(Formatting.WHITE)
                tooltip.add(TranslatableText("lore_book.key_item").formatted(Formatting.WHITE).append(itemText))
            }
            val xpLevels = AugmentHelper.getAugmentImbueLevel(bola)
            tooltip.add(TranslatableText("lore_book.xp_level").formatted(Formatting.WHITE).append(xpLevels.toString()))
            val cooldown = AugmentHelper.getAugmentCooldown(bola).toFloat() / 20.0F
            tooltip.add(TranslatableText("lore_book.cooldown").formatted(Formatting.WHITE).append(LiteralText(cooldown.toString())).append(TranslatableText("lore_book.cooldown1").formatted(Formatting.WHITE)))
            val manaCost = AugmentHelper.getAugmentManaCost(bola)
            tooltip.add(TranslatableText("lore_book.mana_cost").formatted(Formatting.WHITE).append(LiteralText(manaCost.toString())))
            val bole = Registry.ENCHANTMENT.get(Identifier(bola))
            if (bole is ScepterAugment) {
                val spellTier = bole.getTier()
                tooltip.add(
                    TranslatableText("lore_book.tier").formatted(Formatting.WHITE)
                        .append(LiteralText(spellTier.toString()))
                )
            }
        } else {
            addFlavorText(tooltip, context)
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return if (glint) {
            true
        } else {
            super.hasGlint(stack)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (world !is ServerWorld) return TypedActionResult.fail(stack)
        val nbt = stack.orCreateNbt
        if(!nbt.contains(NbtKeys.LORE_KEY.str())){
            val aug = getRandomBookAugment(loreTier.list())
            Nbt.writeStringNbt(NbtKeys.LORE_KEY.str(),aug,nbt)
            world.playSound(null,user.blockPos,SoundEvents.ITEM_BOOK_PAGE_TURN,SoundCategory.NEUTRAL,0.7f,1.0f)
            return TypedActionResult.success(stack)
        } else if (Identifier(Nbt.readStringNbt(NbtKeys.LORE_KEY.str(),nbt)).namespace == "minecraft") {
            val aug = getRandomBookAugment(loreTier.list())
            Nbt.writeStringNbt(NbtKeys.LORE_KEY.str(),aug,nbt)
            world.playSound(null,user.blockPos,SoundEvents.ITEM_BOOK_PAGE_TURN,SoundCategory.NEUTRAL,0.7f,1.0f)
            return TypedActionResult.success(stack)
        }
        return TypedActionResult.pass(stack)
    }

    companion object{

        private fun getRandomBookAugment(list: List<String>): String{
            if (list.isEmpty()) return AC.fallbackId.toString()
            val rndMax = list.size
            val rndIndex = AC.acRandom.nextInt(rndMax)
            return list[rndIndex]
        }

        fun addLoreKeyForREI(stack: ItemStack,augment: String){
            val nbt = stack.orCreateNbt
            if(!nbt.contains(NbtKeys.LORE_KEY.str())) {
                Nbt.writeStringNbt(NbtKeys.LORE_KEY.str(),augment,nbt)
            }
        }

    }
}
