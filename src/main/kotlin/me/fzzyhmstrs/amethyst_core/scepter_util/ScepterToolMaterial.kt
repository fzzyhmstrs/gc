package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.mana_util.ManaItemMaterial
import net.minecraft.item.ToolMaterial

abstract class ScepterToolMaterial: ToolMaterial, ManaItemMaterial {
   
  abstract fun scepterTier(): Int
  
}
