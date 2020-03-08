package com.simibubi.create.foundation.collision;

import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import javax.annotation.Nonnull;
import net.minecraft.util.math.Vec3d;

public class CollisionManager {

  private final ContraptionEntity contraption;

  public CollisionManager(ContraptionEntity contraption) {
    this.contraption = contraption;
  }

  @Nonnull
  public Vec3d tryTranslation(
      double roll, double pitch, double yaw, double prevRoll,
      double prevPitch,
      double prevYaw
                             ) {
    return null;
  }

  public Vec3d tryRotation(
      double roll,
      double pitch,
      double yaw,
      double roll1,
      double pitch1,
      double yaw1
                          ) {
    return null;

  }

}
