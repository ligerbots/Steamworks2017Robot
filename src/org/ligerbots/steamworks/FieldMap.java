package org.ligerbots.steamworks;

/**
 * A map of the field, complete with useful locations and obstacles.
 */
public class FieldMap {
  private static FieldMap red;
  private static FieldMap blue;

  static {
    // +x: blue
    // -x: red
    // +y: boiler
    // -y: feeder
    // 1: boiler side
    // 2: middle
    // 3: feeder side
    // robot starting positions are in the middle of the alliance station
    red = new FieldMap();
    red.starting1 = new FieldPosition(-325.688, 89.060);
    red.starting2 = new FieldPosition(-325.688, 16.475);
    red.starting3 = new FieldPosition(-325.688, -87.003);
    red.boiler = new FieldPosition(-320.133, 155.743);
    red.loadingStationInner = new FieldPosition(311.673, -130.640);
    red.loadingStationOuter = new FieldPosition(268.352, -152.109);
    red.loadingStationOverflow = new FieldPosition(-325.778, -34.191);
    red.hopperBoilerRed = new FieldPosition(-205.203, 157.660);
    red.hopperBoilerCenter = new FieldPosition(0.000, 157.660);
    red.hopperBoilerBlue = new FieldPosition(205.203, 157.600);
    red.hopperLoadingRed = new FieldPosition(-119.243, -157.660);
    red.hopperLoadingBlue = new FieldPosition(119.243, -157.660);
    red.gearLiftStation1 = new FieldPosition(-196.685, 30.000);
    red.gearLiftStation2 = new FieldPosition(-213.171, 0.000);
    red.gearLiftStation3 = new FieldPosition(-196.685, -30.000);
    red.dividerLift12 =
        new FieldLine(new FieldPosition(-212.015, 20.216), new FieldPosition(-232.883, 32.071));
    red.dividerLift23 =
        new FieldLine(new FieldPosition(-212.015, -20.216), new FieldPosition(-232.883, -32.071));
    red.ropeStation1 = new FieldPosition(-146.602, 52.411);
    red.ropeStation2 = new FieldPosition(-237.683, 0);
    red.ropeStation3 = new FieldPosition(-146.602, -52.411);

    blue = new FieldMap();
    blue.starting1 = red.starting1.multiply(-1, 1);
    blue.starting2 = red.starting2.multiply(-1, 1);
    blue.starting3 = red.starting3.multiply(-1, 1);
    blue.boiler = red.boiler.multiply(-1, 1);
    blue.loadingStationInner = red.loadingStationInner.multiply(-1, 1);
    blue.loadingStationOuter = red.loadingStationOuter.multiply(-1, 1);
    blue.loadingStationOverflow = red.loadingStationOverflow.multiply(-1, 1);
    blue.hopperBoilerRed = red.hopperBoilerRed;
    blue.hopperBoilerCenter = red.hopperBoilerCenter;
    blue.hopperBoilerBlue = red.hopperBoilerBlue;
    blue.hopperLoadingRed = red.hopperLoadingRed;
    blue.hopperLoadingBlue = red.hopperLoadingBlue;
    blue.gearLiftStation1 = red.gearLiftStation1.multiply(-1, 1);
    blue.gearLiftStation2 = red.gearLiftStation2.multiply(-1, 1);
    blue.gearLiftStation3 = red.gearLiftStation3.multiply(-1, 1);
    blue.dividerLift12 = red.dividerLift12.multiply(-1, 1);
    blue.dividerLift23 = red.dividerLift23.multiply(-1, 1);
    blue.ropeStation1 = red.ropeStation1.multiply(-1, 1);
    blue.ropeStation2 = red.ropeStation2.multiply(-1, 1);
    blue.ropeStation3 = red.ropeStation3.multiply(-1, 1);
  }

  public FieldMap getRed() {
    return red;
  }

  public FieldMap getBlue() {
    return blue;
  }

  private FieldMap() {}

  FieldPosition starting1;
  FieldPosition starting2;
  FieldPosition starting3;
  FieldPosition boiler;
  FieldPosition loadingStationInner;
  FieldPosition loadingStationOuter;
  FieldPosition loadingStationOverflow;
  FieldPosition hopperBoilerRed;
  FieldPosition hopperBoilerCenter;
  FieldPosition hopperBoilerBlue;
  FieldPosition hopperLoadingRed;
  FieldPosition hopperLoadingBlue;
  FieldPosition gearLiftStation1;
  FieldPosition gearLiftStation2;
  FieldPosition gearLiftStation3;
  FieldLine dividerLift12;
  FieldLine dividerLift23;
  FieldPosition ropeStation1;
  FieldPosition ropeStation2;
  FieldPosition ropeStation3;
}
