package org.ligerbots.steamworks;

public class FieldPosition {
  public static final double FIELD_WIDTH = 654.0;// In inches
  public static final double BOILER_WIDTH = 42.0;// In inches
  public static final double BOILER_HEIGHT = 97.0;// In inches
  public static final double BOILER_HIGH_VISION_TARGET_TOP = 88.0;// In inches
  public static final double BOILER_HIGH_VISION_TARGET_BOTTOM = 84.0;// In inches
  public static final double BOILER_LOW_VISION_TARGET_TOP = 80.0;// In inches
  public static final double BOILER_LOW_VISION_TARGET_BOTTOM = 78.0;// In inches
  public static final double AIRSHIP_WIDTH = 70.5;
  // The approximate distance from the field corner to the boiler corner in inches
  public static final double BOILER_CORNER_OFFSET = 30.0;
  // Distance to baseline from alliance wall in inches
  public static final double ALLIANCE_WALL_TO_BASELINE = 93.25;
  // Distance to launchpad line from alliance wall in inches
  public static final double ALLIANCE_WALL_TO_LAUNCHPAD = ALLIANCE_WALL_TO_BASELINE + AIRSHIP_WIDTH;

  /**
   * The X coordinate of the location in inches. +X is closer to the judges than -X. The origin is
   * at the center of the field.
   */
  @SuppressWarnings("membername")
  private final double x;
  /**
   * The y coordinate of the location in inches. +Y is closer to the blue alliance stations. The
   * origin is at the center of the field.
   */
  @SuppressWarnings("membername")
  private final double y;

  @SuppressWarnings("parametername")
  public FieldPosition(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public FieldPosition add(double dx, double dy) {
    return new FieldPosition(x + dx, y + dy);
  }

  public FieldPosition multiply(double mx, double my) {
    return new FieldPosition(x * mx, y * my);
  }

  public double dot(FieldPosition other) {
    return x * other.getX() + y * other.getY();
  }

  public double magnitude() {
    return Math.sqrt(x * x + y * y);
  }

  public double angleTo(FieldPosition other) {
    return Math.toDegrees(Math.atan2(other.y - y, other.x - x));
  }
  
  /**
   * Calculates the distance to another field position.
   * @param other The other position
   * @return The distance
   */
  public double distanceTo(FieldPosition other) {
    double dx = other.x - x;
    double dy = other.y - y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}
