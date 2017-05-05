package org.ligerbots.steamworks;

public class FieldPosition {
  public static final double FIELD_WIDTH = 654.0;// In inches (x)
  public static final double FIELD_HEIGHT = 330;// In inches (y)
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
  // Distance from center to well past the dividers on the airship (x)
  public static final double CLEAR_DIVIDERS_TO_CENTER = 282;

  /**
   * The x coordinate of the location in inches along the long axis of the field.
   * +x is closer to the blue alliance stations. 
   * The origin is at the center of the field.
   * 
   */
  @SuppressWarnings("membername")
  protected final double x;
  /**
   * The y coordinate of the location in inches along the short axis of the field. 
   * +y is closer to the judges than. 
   * The origin is at the center of the field.
   * 
   */
  @SuppressWarnings("membername")
  protected final double y;

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
  
  public FieldPosition add(FieldPosition other) {
    return add(other.x, other.y);
  }

  public FieldPosition add(double dx, double dy) {
    return new FieldPosition(x + dx, y + dy);
  }
  
  public FieldPosition multiply(double mxy) {
    return multiply(mxy, mxy);
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
  
  @Override
  public String toString() {
    return "FieldPosition [x=" + x + ", y=" + y + "]";
  }
}
