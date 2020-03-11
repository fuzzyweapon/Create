package com.simibubi.create.foundation.collision;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.util.IStringSerializable;

public enum CollisionState implements IStringSerializable {
  INTERSECTING, TOUCHING, NOT_INTERSECTING;

  @Override
  public String getName() {
    return Lang.asId(name());
  }
}
