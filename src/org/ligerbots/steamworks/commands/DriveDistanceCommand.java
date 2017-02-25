package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
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
  
  boolean isHighGear;
  double autoDriveRampUpDist;
  double autoDriveRampDownDist;
  double autoDriveTurnP;
  double autoDriveMaxSpeed;
  double autoDriveMinSpeed;
  double autoDriveStartSpeed;
  
  double nanosSinceOnTarget;
  boolean detectedOnTarget;

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
    
    if (Math.abs(offsetInches) > RobotMap.AUTO_DRIVE_SHIFT_THRESHOLD) {
      Robot.driveTrain.shift(DriveTrain.ShiftType.UP);
      isHighGear = true;
      autoDriveRampUpDist = RobotMap.AUTO_DRIVE_RAMP_UP_DIST_HIGH;
      autoDriveRampDownDist = RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST_HIGH;
      autoDriveTurnP = RobotMap.AUTO_DRIVE_TURN_P_HIGH;
      autoDriveMaxSpeed = RobotMap.AUTO_DRIVE_MAX_SPEED_HIGH;
      autoDriveMinSpeed = RobotMap.AUTO_DRIVE_MIN_SPEED_HIGH;
      autoDriveStartSpeed = RobotMap.AUTO_DRIVE_START_SPEED_HIGH;
    } else {
      Robot.driveTrain.shift(DriveTrain.ShiftType.DOWN);
      isHighGear = false;
      autoDriveRampUpDist = RobotMap.AUTO_DRIVE_RAMP_UP_DIST_LOW;
      autoDriveRampDownDist = RobotMap.AUTO_DRIVE_RAMP_DOWN_DIST_LOW;
      autoDriveTurnP = RobotMap.AUTO_DRIVE_TURN_P_LOW;
      autoDriveMaxSpeed = RobotMap.AUTO_DRIVE_MAX_SPEED_LOW;
      autoDriveMinSpeed = RobotMap.AUTO_DRIVE_MIN_SPEED_LOW;
      autoDriveStartSpeed = RobotMap.AUTO_DRIVE_START_SPEED_LOW;
    }
    
    detectedOnTarget = false;
    
    logger.info(String.format("Initialize, distance=%f, highgear=%b", offsetInches, isHighGear));
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
    double turn = autoDriveTurnP * yawDifference;
    if (turn > 1.0) {
      turn = 1.0;
    } else if (turn < -1.0) {
      turn = -1.0;
    }
    
    boolean onTargetNow = error < RobotMap.AUTO_DRIVE_ACCEPTABLE_ERROR;

    // ramp up, drive at max speed, or ramp down depending on progress (measured by error)
    if (onTargetNow) {
      if (!detectedOnTarget) {
        detectedOnTarget = true;
        nanosSinceOnTarget = System.nanoTime();
        Robot.driveTrain.setHoldPositionEnabled(true);
      }
    } else if (error < autoDriveRampDownDist) {
      double driveSpeed = (error * (autoDriveMaxSpeed - autoDriveMinSpeed)
          / autoDriveRampDownDist) + autoDriveMinSpeed;
      Robot.driveTrain.rawThrottleTurnDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else if (Math.abs(delta) < autoDriveRampUpDist) {
      double driveSpeed =
          (Math.abs(delta) * (autoDriveMaxSpeed - autoDriveStartSpeed)
              / autoDriveRampUpDist) + autoDriveStartSpeed;
      Robot.driveTrain.rawThrottleTurnDrive(offsetInches > 0 ? driveSpeed : -driveSpeed, turn);
    } else {
      Robot.driveTrain.rawThrottleTurnDrive(
          offsetInches > 0 ? autoDriveMaxSpeed : -autoDriveMaxSpeed, turn);
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

    boolean onTarget = detectedOnTarget && System.nanoTime() - nanosSinceOnTarget > 1_000_000_000;
    if (onTarget) {
      succeeded = true;
      ended = true;
    }
    return onTarget;
  }

  protected void end() {
    logger.info("Finish");
    Robot.driveTrain.setHoldPositionEnabled(false);
    Robot.gearManipulator.setOpen(false);
    ended = true;
  }

  protected void interrupted() {
    logger.warn("Interrupted");
    Robot.driveTrain.setHoldPositionEnabled(false);
    Robot.gearManipulator.setOpen(false);
    ended = true;
  }
  
  protected boolean isFailedToComplete() {
    return ended && !succeeded;
  }
}
