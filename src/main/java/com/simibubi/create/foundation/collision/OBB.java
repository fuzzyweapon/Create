package com.simibubi.create.foundation.collision;

import java.util.List;
import java.util.Vector;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.tuple.Pair;

class OBB {

  private List<Vec3d> vertices;
  private Vec3d right;
  private Vec3d up;
  private Vec3d forward;

  OBB(AxisAlignedBB axisAlignedBB, double roll, double yaw, double pitch) {
    Quaternion rotation = Quaternion.eulerAnglesToQuaternion(roll, yaw, pitch).normalized();
    Vec3d center = new Vec3d(axisAlignedBB.getCenter());
    Extents extents = new Extents(axisAlignedBB);

    vertices = new Vector<>(8);
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xPos, extents.yPos, extents.zPos))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xPos, extents.yPos, extents.zNeg))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xPos, extents.yNeg, extents.zPos))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xPos, extents.yNeg, extents.zNeg))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xNeg, extents.yPos, extents.zPos))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xNeg, extents.yPos, extents.zNeg))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xNeg, extents.yNeg, extents.zPos))));
    vertices.add(center.add(rotation.rotate(new Vec3d(extents.xNeg, extents.yNeg, extents.zNeg))));

    right = Vec3d.fromVec3i(Direction.EAST.getDirectionVec());
    up = Vec3d.fromVec3i(Direction.UP.getDirectionVec());
    forward = Vec3d.fromVec3i(Direction.NORTH.getDirectionVec());
  }

  /**
   * Calculate collision data for two intersecting or non-intersecting oriented bounding boxes.
   * <p>
   * When the two OOBs are not colliding or touching, return collision data for caching and a null
   * minimum translation vector.
   * When the two OOBs intersect, return collision data and the minimum translation vector.
   *
   * @return the left {@link VertexCollisionAnalysis} and right, the mtv {@link Vec3d}
   */
  static Pair<VertexCollisionAnalysis, Vec3d> intersects(OBB obb, OBB obb2) {
    VertexCollisionAnalysis separation = separated(obb.vertices, obb2.vertices, obb.right);
    double minOverlap = separation.getOverlap();
    Vec3d minOverlapAxis = separation.getAxis();
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.up);
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.forward);
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }

    separation = separated(obb.vertices, obb2.vertices, obb2.right);
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb2.up);
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb2.forward);
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }

    separation = separated(obb.vertices, obb2.vertices, obb.right.crossProduct(obb2.right));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.right.crossProduct(obb2.up));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.right.crossProduct(obb2.forward));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.up.crossProduct(obb2.right));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.up.crossProduct(obb2.up));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.up.crossProduct(obb2.forward));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }

    separation = separated(obb.vertices, obb2.vertices, obb.forward.crossProduct(obb2.right));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.forward.crossProduct(obb2.up));
    if (separation.getState() == CollisionState.NOT_INTERSECTING) {
      return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
    }
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }
    separation = separated(obb.vertices, obb2.vertices, obb.forward.crossProduct(obb2.forward));
    if (separation.getOverlap() < minOverlap) {
      minOverlap = separation.getOverlap();
      minOverlapAxis = separation.getAxis();
    }

    return Pair.of(separation, minOverlapAxis.multiply(minOverlap));
  }

  private static VertexCollisionAnalysis separated(List<Vec3d> vertices, List<Vec3d> vertices2,
                                                   Vec3d axis) {
    // cross-product will always be zero because they are pointing in the same direction
    if (axis.equals(Vec3d.ZERO)) {
      return new VertexCollisionAnalysis(null, CollisionState.NOT_INTERSECTING);
    }

    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    double min2 = Double.MAX_VALUE;
    double max2 = Double.MIN_VALUE;

    // Define two intervals, a and b. Calculate their min and max values
    for (int i = 0; i < 8; i++) {
      double distance = vertices.get(i).dotProduct(axis);
      min = distance < min ? distance : min;
      max = distance > max ? distance : max;
      double distance2 = vertices2.get(i).dotProduct(axis);
      min2 = distance2 < min2 ? distance2 : min2;
      max2 = distance2 > max2 ? distance2 : max2;
    }

    // One-dimensional intersection test between a and b
    double longSpan = Math.max(max, max2) - Math.max(min, min2);
    double sumSpan = max - min + max2 - min2;
    if (longSpan > sumSpan) {
      return new VertexCollisionAnalysis(axis, CollisionState.INTERSECTING);
    } else if (longSpan < sumSpan) {
      return new VertexCollisionAnalysis(axis, CollisionState.NOT_INTERSECTING);
    } else {
      return new VertexCollisionAnalysis(axis, CollisionState.TOUCHING);
    }
  }

  final private class Extents {

    double xPos, xNeg, yPos, yNeg, zPos, zNeg;

    private Extents(AxisAlignedBB aabb) {
      net.minecraft.util.math.Vec3d center = aabb.getCenter();
      xPos = aabb.maxX - center.getX();
      xNeg = center.getX() - aabb.minX;
      yPos = aabb.maxY - center.getY();
      yNeg = center.getY() - aabb.minY;
      zPos = aabb.maxZ - center.getZ();
      zNeg = center.getZ() - aabb.minZ;
    }

  }

}
