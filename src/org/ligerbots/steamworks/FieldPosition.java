package org.ligerbots.steamworks;

public class FieldPosition {
  /**
   * Which driver station we are starting from. The first driver station is toward the
   */
  public enum StartingPositions {
    POS_STATION_1, POS_STATION_2, POS_STATION_3
  }

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
  
  public FieldPosition multiply(double mx, double my) {
    return new FieldPosition(x * mx, y * my);
  }
}
