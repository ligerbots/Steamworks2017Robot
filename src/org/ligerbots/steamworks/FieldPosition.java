package org.ligerbots.steamworks;

public class FieldPosition {
    private final double x;//The X coordinate of the location in inches. +X is closer to the judges than -X.
    private final double y;//The y coordinate of the location in inches. +Y is closer to the blue alliance stations.
    //The origin is at the center of the field.
    
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
    
}
