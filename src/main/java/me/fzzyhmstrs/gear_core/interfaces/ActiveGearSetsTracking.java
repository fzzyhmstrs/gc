package me.fzzyhmstrs.gear_core.interfaces;

import me.fzzyhmstrs.gear_core.set.GearSet;

import java.util.HashMap;

public interface ActiveGearSetsTracking {
    void markDirty()
    boolean isDirty()
    void gear_core_setActiveSets(HashMap<GearSet, Integer> sets);
    HashMap<GearSet, Integer> gear_core_getActiveSets();
}
