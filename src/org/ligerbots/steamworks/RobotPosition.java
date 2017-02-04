package org.ligerbots.steamworks;

public class RobotPosition extends FieldPosition {
  /**
   * The direction the robot is facing in degrees clockwise, where 0.0 degrees is facing the judges.
   */
  private final double direction;

  @SuppressWarnings("parametername")
  public RobotPosition(double x, double y, double direction) {
    super(x, y);
    this.direction = direction;
  }

  public double getDirection() {
    return direction;
  }
}
