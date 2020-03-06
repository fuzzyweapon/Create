package com.simibubi.create.foundation.collision;

/**
 * {@code DualQuaternion}s are used to represent rotation and translation.
 * <p>
 * Quaternions are great!  However, dual-quaternions are better.
 *
 * @see Quaternion for more info.
 * @see <a href="https://www.3dgep.com/understanding-quaternions/">Quaternion Guide</a>
 * @see <a href="https://cs.gmu.edu/~jmlien/teaching/cs451/uploads/Main/dual-quaternion.pdf">Dual-quaternion
 * Primer</a>
 */
public class DualQuaternion {

  public static final DualQuaternion ZERO = new DualQuaternion(Quaternion.ZERO, Quaternion.ZERO);
  private Quaternion real;
  private Quaternion dual;

  /**
   * Instantiates a new Dual quaternion.
   */
  public DualQuaternion() {
    real = new Quaternion(0f, 0f, 0f, 1f);
    dual = new Quaternion(0f, 0f, 0f, 0f);
  }

  /**
   * Instantiates a new Dual quaternion.
   *
   * @param real the real
   * @param dual the dual
   */
  private DualQuaternion(Quaternion real, Quaternion dual) {
    this.real = Quaternion.normalized(real);
    this.dual = dual;
  }

  /**
   * Instantiates a new Dual quaternion.
   *
   * @param quaternion the quaternion
   * @param vector the vector
   */
  public DualQuaternion(Quaternion quaternion, Vec3d vector) {
    real = Quaternion.normalized(quaternion);
    dual = new Quaternion(0.0D, vector).multiply(real).multiply(0.5D);
  }

  private void multiply(double factor) {
    real = real.multiply(factor);
    dual = dual.multiply(factor);
  }

  public final DualQuaternion multiply(DualQuaternion dualQuaternion) {
    // Multiply left to right
    return new DualQuaternion(
        dualQuaternion.real.multiply(real),
        dualQuaternion.dual.multiply(real).add(dualQuaternion.real.multiply(dual))
    );
  }

  private DualQuaternion add(DualQuaternion dualQuaternion) {
    return new DualQuaternion(real.add(dualQuaternion.real), dual.add(dualQuaternion.dual));
  }

  private DualQuaternion normalize() {
    double mag = this.magnitude();
    assert (mag > 0.000001D);

    double factor = 1.0 / mag;
    real = real.multiply(factor);
    dual = dual.multiply(factor);
    return this;
  }

  private double magnitude() {
    return real.dotProduct(real);
  }

  private double dotProduct(DualQuaternion quaternion) {
    return real.dotProduct(quaternion.real);
  }

  private DualQuaternion conjugate() {
    return new DualQuaternion(real.conjugate(), dual.conjugate());
  }

  private Quaternion getRotation() {
    return real;
  }

  private Vec3d getTranslation() {
    Quaternion translation = (dual.multiply(2.0D)).multiply(real.conjugate());
    return new Vec3d(translation.getX(), translation.getY(), translation.getZ());
  }

}