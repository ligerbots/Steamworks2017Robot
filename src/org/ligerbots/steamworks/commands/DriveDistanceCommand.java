package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain.DriveTrainSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drives straight a certain distance (in inches).
 */
public class DriveDistanceCommand extends AccessibleCommand {
  private static final Logger logger = LoggerFactory.getLogger(DriveDistanceCommand.class);

  double offsetInches;

  double startLeftEncoderValue;
  double startRightEncoderValue;
  double startYaw;

  double delta;
  double error;

  boolean ended;
  boolean succeeded;

  /**
   * Create a new DriveDistanceCommand.
   * 
   * @param offsetInches The number of inches to drive.
   */
  public DriveDistanceCommand(double offsetInches) {
    super("DriveDistanceCommand_" + offsetInches);
    this.offsetInches = offsetInches;
    requires(Robot.driveTrain);
  }

  protected void initialize() {
    startLeftEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
    startRightEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    startYaw = Robot.driveTrain.getYaw();
    succeeded = false;
    ended = false;
    logger.info(String.format("Initialize, distance=%f", offsetInches));
  }

  protected void execute() {
    // find the encoder average delta since initialize()
    double currentLeftEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.LEFT);
    double currentRightEncoderValue = Robot.driveTrain.getEncoderDistance(DriveTrainSide.RIGHT);
    delta = ((currentLeftEncoderValue - startLeftEncoderValue)
        + (currentRightEncoderValue - startRightEncoderValue)) / 2;
    // drive error
    error = Math.abs(delta - offsetInches);
    // turn error
    double yawDifference;
    double currentYaw = Robot.driveTrain.getYaw();
    // handle wrap around 360
    if (Math.abs(currentYaw - startYaw) < 180) {
      yawDifference = currentYaw - startYaw;
    } else {
      yawDifference = 360 - Math.abs(currentYaw - startYaw);
      if (currentYaw > startYaw) {
        yawDifference = -yawDifference;
      }
    }

    logger.info(String.format("left: %f, right: %f, delta: %f, error: %f, yawError: %f",
        currentLeftEncoderValue, currentRightEncoderValue, delta, error, yawDifference));

    // turn to fix yaw error
    double turn = RobotMap.AUTO_DRIVE_TURN_P * yawDifference;
    if (turn > 1.0) {
      turn = 1.0;
    } else if (turn < -1.0) {
      turn = -1.0;
    }

    // ramp up, drive at max speed, or ramp down depending on progress (measured by error)
    if (error < RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST) {
      double driveSpeed = (error * (RobotMap.AUTO_DRIVE_MAX_SPEED - RobotMap.AUTO_DRIVE_MIN_SPEED)
          / RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST) + RobotMap.AUTO_DRIVE_MIN_SPEED;
      Robot.driveTrain.rawThrottleTurnDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else if (Math.abs(delta) < RobotMap.AUTO_DRIVE_RAMP_UP_DIST) {
      double driveSpeed =
          (Math.abs(delta) * (RobotMap.AUTO_DRIVE_MAX_SPEED - RobotMap.AUTO_DRIVE_START_SPEED)
              / RobotMap.AUTO_DRIVE_RAMP_UP_DIST) + RobotMap.AUTO_DRIVE_START_SPEED;
      Robot.driveTrain.rawThrottleTurnDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else {
      Robot.driveTrain.rawThrottleTurnDrive(
          offsetInches > 0 ? RobotMap.AUTO_DRIVE_MAX_SPEED : -RobotMap.AUTO_DRIVE_MAX_SPEED, turn);
    }
  }

  protected boolean isFinished() {
    // if we passed the target, just stop
    if ((offsetInches < 0 && delta < offsetInches) || (offsetInches > 0 && delta > offsetInches)) {
      succeeded = true;
      ended = true;
      return true;
    }

    if (Robot.operatorInterface.isCancelled()) {
      ended = true;
      return true;
    }

    boolean onTarget = error < RobotMap.AUTO_DRIVE_ACCEPTABLE_ERROR;
    if (onTarget) {
      succeeded = true;
      ended = true;
    }
    return onTarget;
  }

  protected void end() {
    logger.info("Finish");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    Robot.gearManipulator.setOpen(false);
  }

  protected void interrupted() {
    logger.warn("Interrupted");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    Robot.gearManipulator.setOpen(false);
  }
  
  protected boolean isFailedToComplete() {
    return ended && !succeeded;
  }
}
