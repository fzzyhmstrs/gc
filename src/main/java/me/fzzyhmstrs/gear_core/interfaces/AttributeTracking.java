package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public interface AttributeTracking {
  
  default boolean fzzy_core_correctSlot(EquipmentSlot slot){
      return false;
  }
  
  @Nullable
  default EquipmentSlot fzzy_core_getCorrectSlot(){
      return null;
  }
}
