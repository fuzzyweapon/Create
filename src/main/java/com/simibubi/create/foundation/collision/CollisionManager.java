package com.simibubi.create.foundation.collision;

import com.simibubi.create.foundation.utility.NotImplementedException;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import javax.annotation.Nonnull;

public class CollisionManager {

  private final ContraptionEntity contraption;

  public CollisionManager(ContraptionEntity contraption) {
    this.contraption = contraption;
  }

  @Nonnull
  public Vec3d tryTranslation(double prevX, double prevY, double prevZ, double x,
                              double y, double z) {
    try {
      throw new NotImplementedException();
    } catch (NotImplementedException e) {
      e.printStackTrace();
    }
    return Vec3d.ZERO;
  }

  private double calculateConvergence(final Vec3d contraptionEntityMotion,
                                      final Vec3d neighborMotion) {

    return contraptionEntityMotion.dotProduct(neighborMotion);
  }

  public Vec3d tryRotation(double prevRoll, double prevYaw, double prevPitch,
                           double roll, double yaw, double pitch) {
    return null;
  }

}
