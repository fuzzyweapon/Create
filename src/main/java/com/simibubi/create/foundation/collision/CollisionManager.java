package com.simibubi.create.foundation.collision;

import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

public class CollisionManager {

  private final ContraptionEntity contraptionEntity;
  private List<Pair<Entity, Double>> convergingEntities;

  public CollisionManager(ContraptionEntity contraption) {

    this.contraptionEntity = contraption;
    this.convergingEntities = new ArrayList<>();
  }

  @Nonnull
  public Vec3d maybeTranslate(double prevX, double prevY, double prevZ, double x,
                              double y, double z) {
    return tryMove(prevX, prevY, prevZ, x,
                   y, z, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
  }

  @Nonnull
  public Vec3d tryMove(double prevX, double prevY, double prevZ, double x, double y, double z,
                       double prevRoll, double prevYaw, double prevPitch, double roll, double yaw,
                       double pitch) {

    AxisAlignedBB aabb = contraptionEntity.getBoundingBox();
    Vec3d contraptionEntityMotion = contraptionEntity.getMotion();
    AxisAlignedBB neighborAABB;
    Vec3d neighborMotion = Vec3d.ZERO;
    double convergence;

    final List<Entity> entitiesInAABBexcluding = contraptionEntity.world.getEntitiesInAABBexcluding(
        contraptionEntity,
        aabb.grow(1.0D), null);
    for (final Entity neighbor : entitiesInAABBexcluding) {
      convergence = calculateConvergence(contraptionEntityMotion, neighborMotion);
      if (convergence > 0.0D) {
        neighborAABB = neighbor.getBoundingBox();
        convergingEntities.add(Pair.of(neighbor, convergence));
        OBB contraptionOBB = new OBB(aabb, roll, yaw, pitch);
        OBB neighborOBB;

        if (neighbor instanceof ContraptionEntity) {
          ContraptionEntity neighborContraptionEntity = (ContraptionEntity) neighbor;
          neighborOBB = new OBB(neighborAABB, neighborContraptionEntity.roll,
                                neighborContraptionEntity.yaw, neighborContraptionEntity.pitch);
        } else {
          neighborOBB = new OBB(neighborAABB, 0.0D, neighbor.rotationYaw, neighbor.rotationPitch);
        }

        Pair<VertexCollisionAnalysis, com.simibubi.create.foundation.collision.Vec3d>
            intersectData = OBB.intersects(contraptionOBB, neighborOBB);
        com.simibubi.create.foundation.collision.Vec3d
            minimumTranslationVector = intersectData.getRight();

        if (minimumTranslationVector != null) {
          contraptionEntity.setMotion(Vec3d.ZERO);
          contraptionEntity.getContraption().stalled = true;
          return new Vec3d(x, y, z).add(neighbor.posX, neighbor.posY, neighbor.posZ);
        }
      }
    }
    return Vec3d.ZERO;
  }

  // TODO:  Take into account floating-point error?
  // Question: If we use this with magnitude, can we short circuit out?
  private double calculateConvergence(final Vec3d contraptionEntityMotion,
                                      final Vec3d neighborMotion) {
    if (!(contraptionEntityMotion.equals(Vec3d.ZERO) && neighborMotion.equals(Vec3d.ZERO))) {
      return contraptionEntityMotion.dotProduct(neighborMotion);
    }
    return 0.0D;
  }

  public Vec3d maybeRotate(double prevRoll, double prevYaw, double prevPitch, double roll,
                           double yaw, double pitch) {
    return tryMove(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, prevRoll,
                   prevYaw, prevPitch, roll, yaw, pitch);
  }

}
