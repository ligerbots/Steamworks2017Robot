package org.ligerbots.steamworks;

public class RobotPosition extends FieldPosition {
  private final double direction;//The direction the robot is facing in degrees clockwise, where 0.0 degrees is facing the judges.
  public RobotPosition(double x, double y, double direction) {
    super(x, y);
    this.direction = direction;
  }
  public double getDirection() {
    return direction;
  }
}
