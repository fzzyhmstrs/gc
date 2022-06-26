package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.item.Item
import net.minecraft.item.Items

data class AugmentDatapoint(val type: SpellType = SpellType.NULL, val cooldown: Int = 20,
                            val manaCost: Int = 20, val minLvl: Int = 1, val imbueLevel: Int = 1,
                            val bookOfLoreTier: LoreTier = LoreTier.NO_TIER, val keyItem: Item = Items.AIR
)