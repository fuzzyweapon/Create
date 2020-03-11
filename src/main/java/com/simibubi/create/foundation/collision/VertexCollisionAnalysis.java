package com.simibubi.create.foundation.collision;

final class VertexCollisionAnalysis {

  private double overlap;
  private Vec3d axis;
  private Vec3d vertex;
  private CollisionState state;

  public VertexCollisionAnalysis(final Vec3d axis, final CollisionState state) {
    this.vertex = vertex;
    this.axis = axis;
    this.state = state;
  }

  CollisionState getState() {
    return state;
  }

  double getOverlap() {
    return overlap;
  }

  Vec3d getAxis() {
    return axis;
  }

}
