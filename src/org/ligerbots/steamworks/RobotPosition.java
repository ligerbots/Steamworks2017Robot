package org.ligerbots.steamworks;

public class RobotPosition extends FieldPosition {
  /**
   * The direction the robot is facing in degrees clockwise, where 0.0 degrees is facing the judges.
   */
  protected final double direction;

  @SuppressWarnings("parametername")
  public RobotPosition(double x, double y, double direction) {
    super(x, y);
    this.direction = direction;
  }

  public double getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return "RobotPosition [direction=" + direction + ", x=" + x + ", y=" + y + "]";
  }
}
