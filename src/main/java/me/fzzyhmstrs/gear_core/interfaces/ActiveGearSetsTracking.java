package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface ActiveGearSetsTracking {
    void setActiveSets(Set<GearSet> sets);
    Set<GearSet> getActiveSets();
}
