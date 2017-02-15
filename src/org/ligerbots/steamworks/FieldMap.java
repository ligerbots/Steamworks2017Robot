package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.commands.AccessibleCommand;
import org.ligerbots.steamworks.commands.DriveDistanceCommand;
import org.ligerbots.steamworks.commands.TurnCommand;
import org.ligerbots.steamworks.commands.WaitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A map of the field, complete with useful locations and obstacles.
 */
public class FieldMap {
  private static final Logger logger = LoggerFactory.getLogger(FieldMap.class);

  public static final int FIELD_SIDE_BOILER = 0;
  public static final int FIELD_SIDE_CENTER = 1;
  public static final int FIELD_SIDE_FEEDER = 2;

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

  public FieldPosition[] startingPositions = new FieldPosition[3];
  public FieldPosition boiler;
  public FieldPosition loadingStationInner;
  public FieldPosition loadingStationOuter;
  public FieldPosition loadingStationOverflow;
  public FieldPosition hopperBoilerRed;
  public FieldPosition hopperBoilerCenter;
  public FieldPosition hopperBoilerBlue;
  public FieldPosition hopperLoadingRed;
  public FieldPosition hopperLoadingBlue;
  public FieldPosition[] gearLiftPositions = new FieldPosition[3];
  public FieldLine dividerLift12;
  public FieldLine dividerLift23;
  public FieldPosition ropeStation1;
  public FieldPosition ropeStation2;
  public FieldPosition ropeStation3;

  public static class Navigation {
    public List<AccessibleCommand> commands = new LinkedList<>();
    public int commandIndex = 0;
  }

  /**
   * Calculates the navigation steps to go to the gear lift.
   * 
   * @param startingPositionId The starting position ID (see comment at top of file), 0-2
   * @param gearLiftPositionId The gear lift position ID, 0-2
   * @return A Navigation object with the steps required
   */
  public static Navigation navigateStartToGearLift(int startingPositionId, int gearLiftPositionId) {
    logger.info(String.format("Calculating path, start=%d, gear=%d", startingPositionId,
        gearLiftPositionId));
    FieldMap map = getAllianceMap();

    final Alliance alliance = DriverStation.getInstance().getAlliance();

    if (startingPositionId < 0 || startingPositionId >= map.startingPositions.length) {
      startingPositionId = 0;
      logger.error("Bad starting position: " + startingPositionId);
    }

    if (gearLiftPositionId < 0 || gearLiftPositionId >= map.gearLiftPositions.length) {
      gearLiftPositionId = 0;
      logger.error("Bad gear lift position: " + gearLiftPositionId);
    }

    FieldPosition startingPosition = map.startingPositions[startingPositionId];
    logger.debug(String.format("Starting position %s", startingPosition));
    FieldPosition gearLiftPosition = map.gearLiftPositions[gearLiftPositionId];

    // drive forward 2 feet
    FieldPosition initialForwardPosition =
        startingPosition.add(alliance == Alliance.Red ? 24 : -24, 0);
    logger.debug(String.format("2ft forward position %s", initialForwardPosition));

    double initialDriveToX;
    double initialDriveToY;
    if (gearLiftPositionId == 1) {
      initialDriveToX = alliance == Alliance.Red ? -276 : 276;
      initialDriveToY = 0;
    } else if (gearLiftPositionId == 0) {
      double angle = alliance == Alliance.Red ? 120 : 60;
      double dx = 77 * Math.cos(Math.toRadians(angle));
      double dy = 77 * Math.sin(Math.toRadians(angle));
      initialDriveToX = gearLiftPosition.getX() + dx;
      initialDriveToY = gearLiftPosition.getY() + dy;
    } else {
      double angle = alliance == Alliance.Red ? 240 : 300;
      double dx = 77 * Math.cos(Math.toRadians(angle));
      double dy = 77 * Math.sin(Math.toRadians(angle));
      initialDriveToX = gearLiftPosition.getX() + dx;
      initialDriveToY = gearLiftPosition.getY() + dy;
    }

    FieldPosition initialDriveToPosition = new FieldPosition(initialDriveToX, initialDriveToY);
    logger.debug(String.format("Drive to position %s", initialDriveToPosition));

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
    logger.debug(String.format("Turn angle %f", turnAngle));
    final TurnCommand turnToPosition = new TurnCommand(turnAngle);

    double distance = initialForwardPosition.distanceTo(initialDriveToPosition);
    final DriveDistanceCommand driveToPosition = new DriveDistanceCommand(distance);
    logger.debug(String.format("Distance %f", distance));

    double resetTurnAngle = -turnAngle;
    if (gearLiftPositionId == 0) {
      resetTurnAngle += 60;
    }
    if (gearLiftPositionId == 2) {
      resetTurnAngle -= 60;
    }

    logger.debug(String.format("Back turn angle %f", resetTurnAngle));

    final TurnCommand turnToTarget = new TurnCommand(resetTurnAngle);

    Navigation navigation = new Navigation();
    navigation.commands.add(driveForward);
    navigation.commands.add(turnToPosition);
    navigation.commands.add(driveToPosition);
    navigation.commands.add(turnToTarget);
    navigation.commands.add(new WaitCommand(250_000_000L));
    return navigation;
  }

  /**
   * Given a current dead reckoned position, navigates to the boiler.
   * @param currentPosition The current robot position
   * @return A Navigation object with the steps required
   */
  public static Navigation navigateToBoiler(RobotPosition currentPosition) {
    logger.info(String.format("Calculating path, start=%s", currentPosition));
    FieldMap map = getAllianceMap();

    FieldPosition boiler = map.boiler;

    double absoluteAngle = currentPosition.angleTo(boiler);
    double relativeAngle = (absoluteAngle - currentPosition.direction + 360) % 360;
    if (relativeAngle > 180) {
      relativeAngle = relativeAngle - 360;
    }

    TurnCommand turnToBoiler = new TurnCommand(relativeAngle);

    double distanceToBoiler = currentPosition.distanceTo(boiler);

    Navigation navigation = new Navigation();
    navigation.commands.add(turnToBoiler);

    if (distanceToBoiler > 15 * 60) {
      navigation.commands.add(
          new DriveDistanceCommand(distanceToBoiler - RobotMap.MAXIMUM_SHOOTING_DISTANCE + 10.0));
    }

    navigation.commands.add(new WaitCommand(250_000_000L));
    return navigation;
  }
}
