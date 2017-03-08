package org.ligerbots.steamworks.commands;

import java.util.List;
import org.ligerbots.steamworks.FieldPosition;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.RobotPosition;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows a set of waypoints.
 */
public class DrivePathCommand extends AccessibleCommand {
  private static final Logger logger = LoggerFactory.getLogger(DrivePathCommand.class);

  List<FieldPosition> waypoints;
  int waypointIndex;
  double startAbsDistance;
  boolean driveForward;
  double angleError;

  RobotPosition currentPosition;
  FieldPosition currentWaypoint;

  boolean isHighGear;
  double autoDriveRampUpDist;
  double autoDriveRampDownDist;
  double autoDriveTurnP;
  double autoDriveMaxSpeed;
  double autoDriveMinSpeed;
  double autoDriveStartSpeed;
  double autoTurnMaxSpeed;

  /**
   * Creates a new PathDrivingCommand.
   * 
   * @param waypoints The set of waypoints to follow
   */
  public DrivePathCommand(List<FieldPosition> waypoints) {
    requires(Robot.driveTrain);

    this.waypoints = waypoints;
  }
  
  protected void initialize() {
    logger.info(String.format("Initialize: %s", waypoints.toString()));

    waypointIndex = 0;
    startAbsDistance = Robot.driveTrain.getAbsoluteDistanceTraveled();

    driveForward = true;

    // no idea how high gear would work / no testing time
    isHighGear = false;

    logger.info(String.format("High gear: %b", isHighGear));

    if (isHighGear) {
      Robot.driveTrain.shift(DriveTrain.ShiftType.UP);
      autoDriveRampUpDist = RobotMap.AUTO_DRIVE_RAMP_UP_DIST_HIGH;
      autoDriveRampDownDist = RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST_HIGH;
      autoDriveTurnP = RobotMap.AUTO_DRIVE_TURN_P_HIGH;
      autoDriveMaxSpeed = RobotMap.AUTO_DRIVE_MAX_SPEED_HIGH;
      autoDriveMinSpeed = RobotMap.AUTO_DRIVE_MIN_SPEED_HIGH;
      autoDriveStartSpeed = RobotMap.AUTO_DRIVE_START_SPEED_HIGH;
      autoTurnMaxSpeed = RobotMap.AUTO_TURN_MAX_SPEED_HIGH;
    } else {
      Robot.driveTrain.shift(DriveTrain.ShiftType.DOWN);
      autoDriveRampUpDist = RobotMap.AUTO_DRIVE_RAMP_UP_DIST_LOW;
      autoDriveRampDownDist = RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST_LOW;
      autoDriveTurnP = RobotMap.AUTO_DRIVE_TURN_P_LOW;
      autoDriveMaxSpeed = RobotMap.AUTO_DRIVE_MAX_SPEED_LOW;
      autoDriveMinSpeed = RobotMap.AUTO_DRIVE_MIN_SPEED_LOW;
      autoDriveStartSpeed = RobotMap.AUTO_DRIVE_START_SPEED_LOW;
      autoTurnMaxSpeed = RobotMap.AUTO_TURN_MAX_SPEED_LOW;
    }
  }

  protected void execute() {
    currentPosition = Robot.driveTrain.getRobotPosition();

    // TODO: inefficient, do we care?
    double minDist = Double.MAX_VALUE;
    int minDistPointIndex = waypointIndex;
    for (int i = 0; i < waypoints.size(); i++) {
      double dist = waypoints.get(i).distanceTo(currentPosition);
      if (dist < minDist) {
        minDist = dist;
        minDistPointIndex = i;
      }
    }

    if (minDist < Double.MAX_VALUE) {
      waypointIndex = minDistPointIndex + 5;
      if (waypointIndex >= waypoints.size()) {
        waypointIndex = waypoints.size() - 1;
      }
    }

    currentWaypoint = waypoints.get(waypointIndex);

    double angleToWaypoint = 90 - currentPosition.angleTo(currentWaypoint);
    angleError = (angleToWaypoint - currentPosition.getDirection() + 360) % 360;
    if (angleError > 180) {
      angleError -= 360;
    }

    logger.debug(String.format("angleTo=%f current=%f error=%f currentPos=%s waypoints[%d]=%s",
        angleToWaypoint, currentPosition.getDirection(), angleError, currentPosition.toString(),
        waypointIndex, currentWaypoint.toString()));

    double turn = -autoDriveTurnP * angleError;
    if (turn > autoTurnMaxSpeed) {
      turn = autoTurnMaxSpeed;
    } else if (turn < -autoTurnMaxSpeed) {
      turn = -autoTurnMaxSpeed;
    }

    double rampUpDelta = Robot.driveTrain.getAbsoluteDistanceTraveled() - startAbsDistance;
    double rampDownDelta = currentPosition.distanceTo(waypoints.get(waypoints.size() - 1));

    // ramp up, drive at max speed, or ramp down depending on progress (measured by error)
    if (rampDownDelta < autoDriveRampDownDist) {
      double driveSpeed =
          (rampDownDelta * (autoDriveMaxSpeed - autoDriveMinSpeed) / autoDriveRampDownDist)
              + autoDriveMinSpeed;

      if (Math.abs(angleError) > 30) {
        driveSpeed = 0;
      }

      Robot.driveTrain.rawThrottleTurnDrive(driveForward ? driveSpeed : -driveSpeed, turn);
    } else if (rampUpDelta < autoDriveRampUpDist) {
      double driveSpeed =
          (Math.abs(rampUpDelta) * (autoDriveMaxSpeed - autoDriveStartSpeed) / autoDriveRampUpDist)
              + autoDriveStartSpeed;

      if (Math.abs(angleError) > 30) {
        driveSpeed = 0;
      }

      Robot.driveTrain.rawThrottleTurnDrive(driveForward ? driveSpeed : -driveSpeed, turn);
    } else {
      if (Math.abs(angleError) > 30) {
        Robot.driveTrain.rawThrottleTurnDrive(0, turn);
      } else {
        Robot.driveTrain.rawThrottleTurnDrive(driveForward ? autoDriveMaxSpeed : -autoDriveMaxSpeed,
            turn);
      }
    }
  }

  protected boolean isFinished() {
    return waypointIndex >= waypoints.size() - 1
        && currentPosition
            .distanceTo(waypoints.get(waypoints.size() - 1)) < RobotMap.AUTO_DRIVE_ACCEPTABLE_ERROR;
  }

  protected void end() {
    logger.info("Finish");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }

  protected void interrupted() {
    logger.warn("Interrupted");

    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
  }
}
