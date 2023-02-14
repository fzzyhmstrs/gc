package me.fzzyhmstrs.gear_core.interfaces;

import net.minecraft.entity.EquipmentSlot;

public interface AttributeTracking {
  
  default boolean correctSlot(EquipmentSlot slot){
      return false;
  }
  
  @Nullable
  default EquipmentSlot getCorrectSlot(){
      return null;
  }
}
