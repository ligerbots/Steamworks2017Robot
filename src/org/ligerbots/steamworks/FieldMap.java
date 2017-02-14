package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.commands.AccessibleCommand;
import org.ligerbots.steamworks.commands.DriveDistanceCommand;
import org.ligerbots.steamworks.commands.TurnCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A map of the field, complete with useful locations and obstacles.
 */
public class FieldMap {
  private static final Logger logger = LoggerFactory.getLogger(FieldMap.class);

  private static FieldMap red;
  private static FieldMap blue;

  static {
    // +x: blue
    // -x: red
    // +y: boiler
    // -y: feeder
    // 0: boiler side
    // 1: middle
    // 2: feeder side
    // robot starting positions are in the middle of the alliance station
    red = new FieldMap();
    red.startingPositions[0] = new FieldPosition(-325.688, 89.060);
    red.startingPositions[1] = new FieldPosition(-325.688, 16.475);
    red.startingPositions[2] = new FieldPosition(-325.688, -87.003);
    red.boiler = new FieldPosition(-320.133, 155.743);
    red.loadingStationInner = new FieldPosition(311.673, -130.640);
    red.loadingStationOuter = new FieldPosition(268.352, -152.109);
    red.loadingStationOverflow = new FieldPosition(-325.778, -34.191);
    red.hopperBoilerRed = new FieldPosition(-205.203, 157.660);
    red.hopperBoilerCenter = new FieldPosition(0.000, 157.660);
    red.hopperBoilerBlue = new FieldPosition(205.203, 157.600);
    red.hopperLoadingRed = new FieldPosition(-119.243, -157.660);
    red.hopperLoadingBlue = new FieldPosition(119.243, -157.660);
    red.gearLiftPositions[0] = new FieldPosition(-196.685, 30.000);
    red.gearLiftPositions[1] = new FieldPosition(-213.171, 0.000);
    red.gearLiftPositions[2] = new FieldPosition(-196.685, -30.000);
    red.dividerLift12 =
        new FieldLine(new FieldPosition(-212.015, 20.216), new FieldPosition(-232.883, 32.071));
    red.dividerLift23 =
        new FieldLine(new FieldPosition(-212.015, -20.216), new FieldPosition(-232.883, -32.071));
    red.ropeStation1 = new FieldPosition(-146.602, 52.411);
    red.ropeStation2 = new FieldPosition(-237.683, 0);
    red.ropeStation3 = new FieldPosition(-146.602, -52.411);

    blue = new FieldMap();
    blue.startingPositions[0] = red.startingPositions[0].multiply(-1, 1);
    blue.startingPositions[1] = red.startingPositions[1].multiply(-1, 1);
    blue.startingPositions[2] = red.startingPositions[2].multiply(-1, 1);
    blue.boiler = red.boiler.multiply(-1, 1);
    blue.loadingStationInner = red.loadingStationInner.multiply(-1, 1);
    blue.loadingStationOuter = red.loadingStationOuter.multiply(-1, 1);
    blue.loadingStationOverflow = red.loadingStationOverflow.multiply(-1, 1);
    blue.hopperBoilerRed = red.hopperBoilerRed;
    blue.hopperBoilerCenter = red.hopperBoilerCenter;
    blue.hopperBoilerBlue = red.hopperBoilerBlue;
    blue.hopperLoadingRed = red.hopperLoadingRed;
    blue.hopperLoadingBlue = red.hopperLoadingBlue;
    blue.gearLiftPositions[0] = red.gearLiftPositions[0].multiply(-1, 1);
    blue.gearLiftPositions[1] = red.gearLiftPositions[1].multiply(-1, 1);
    blue.gearLiftPositions[2] = red.gearLiftPositions[2].multiply(-1, 1);
    blue.dividerLift12 = red.dividerLift12.multiply(-1, 1);
    blue.dividerLift23 = red.dividerLift23.multiply(-1, 1);
    blue.ropeStation1 = red.ropeStation1.multiply(-1, 1);
    blue.ropeStation2 = red.ropeStation2.multiply(-1, 1);
    blue.ropeStation3 = red.ropeStation3.multiply(-1, 1);
  }

  public static FieldMap getRed() {
    return red;
  }

  public static FieldMap getBlue() {
    return blue;
  }

  /**
   * Gets the current alliance FieldMap.
   * 
   * @return Either red map or blue map
   */
  public static FieldMap getAllianceMap() {
    Alliance alliance = DriverStation.getInstance().getAlliance();
    if (alliance == Alliance.Blue) {
      return getBlue();
    } else if (alliance == Alliance.Red) {
      return getRed();
    } else {
      logger.error("Invalid alliance reported by DS!");
      return getBlue();
    }
  }

  private FieldMap() {}

  FieldPosition[] startingPositions = new FieldPosition[3];
  FieldPosition boiler;
  FieldPosition loadingStationInner;
  FieldPosition loadingStationOuter;
  FieldPosition loadingStationOverflow;
  FieldPosition hopperBoilerRed;
  FieldPosition hopperBoilerCenter;
  FieldPosition hopperBoilerBlue;
  FieldPosition hopperLoadingRed;
  FieldPosition hopperLoadingBlue;
  FieldPosition[] gearLiftPositions = new FieldPosition[3];
  FieldLine dividerLift12;
  FieldLine dividerLift23;
  FieldPosition ropeStation1;
  FieldPosition ropeStation2;
  FieldPosition ropeStation3;

  public static class Navigation {
    public List<AccessibleCommand> commands = new LinkedList<>();
    public int commandIndex = 0;
  }
  
  /**
   * Calculates the navigation steps to go to the gear lift.
   * @param startingPositionId The starting position ID (see comment at top of file), 0-2
   * @param gearLiftPositionId The gear lift position ID, 0-2
   * @return A Navigation object with the steps required
   */
  public static Navigation navigateStartToGearLift(int startingPositionId, int gearLiftPositionId) {
    FieldMap map = getAllianceMap();

    Alliance alliance = DriverStation.getInstance().getAlliance();

    if (startingPositionId < 0 || startingPositionId >= map.startingPositions.length) {
      startingPositionId = 0;
      logger.error("Bad starting position: " + startingPositionId);
    }

    if (gearLiftPositionId < 0 || gearLiftPositionId >= map.gearLiftPositions.length) {
      gearLiftPositionId = 0;
      logger.error("Bad gear lift position: " + gearLiftPositionId);
    }

    FieldPosition startingPosition = map.startingPositions[startingPositionId];
    // FieldPosition gearLiftPosition = map.gearLiftPositions[gearLiftPositionId];

    // drive forward 2 feet
    FieldPosition initialForwardPosition =
        startingPosition.add(alliance == Alliance.Red ? 24 : -24, 0);

    double initialDriveToX;
    double initialDriveToY;
    if (gearLiftPositionId == 1) {
      initialDriveToX = alliance == Alliance.Red ? -276 : 276;
      initialDriveToY = 0;
    } else {
      initialDriveToX = alliance == Alliance.Red ? -260 : 260;
      initialDriveToY = gearLiftPositionId == 0 ? 87 : -87;
    }

    FieldPosition initialDriveToPosition = new FieldPosition(initialDriveToX, initialDriveToY);

    final DriveDistanceCommand driveForward = new DriveDistanceCommand(24.0);
    double angleTo = initialForwardPosition.angleTo(initialDriveToPosition);
    double turnAngle;

    if (alliance == Alliance.Red) {
      if (angleTo > 180) {
        turnAngle = 360 - angleTo;
      } else {
        turnAngle = -angleTo;
      }
    } else {
      turnAngle = 180 - angleTo;
    }
    final TurnCommand turnToPosition = new TurnCommand(turnAngle);

    final DriveDistanceCommand driveToPosition =
        new DriveDistanceCommand(initialForwardPosition.distanceTo(initialDriveToPosition));
    
    double resetTurnAngle = -turnAngle;
    if (gearLiftPositionId == 0) {
      resetTurnAngle += 60;
    }
    if (gearLiftPositionId == 2) {
      resetTurnAngle -= 60;
    }
    
    final TurnCommand turnToTarget = new TurnCommand(resetTurnAngle);
    
    Navigation navigation = new Navigation();
    navigation.commands.add(driveForward);
    navigation.commands.add(turnToPosition);
    navigation.commands.add(driveToPosition);
    navigation.commands.add(turnToTarget);
    return navigation;
  }
}
