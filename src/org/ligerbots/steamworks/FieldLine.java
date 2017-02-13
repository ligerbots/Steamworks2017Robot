package org.ligerbots.steamworks;

/**
 * Represents a line on the field. For example, the dividers.
 */
public class FieldLine {
  private final FieldPosition start;
  private final FieldPosition end;
  
  /**
   * Creates a new FieldLine.
   * @param start The start point
   * @param end The end point
   */
  public FieldLine(FieldPosition start, FieldPosition end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Gives the start point.
   * @return the start
   */
  public FieldPosition getStart() {
    return start;
  }

  /**
   * Gives the end point.
   * @return the end
   */
  public FieldPosition getEnd() {
    return end;
  }
  
  public FieldLine multiply(double mx, double my) {
    return new FieldLine(start.multiply(mx, my), end.multiply(mx, my));
  }
}
