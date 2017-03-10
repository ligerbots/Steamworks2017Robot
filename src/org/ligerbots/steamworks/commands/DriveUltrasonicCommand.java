package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import java.util.Arrays;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drives until the ultrasonic sensor reads the target distance.
 */
public class DriveUltrasonicCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(DriveUltrasonicCommand.class);

  double distLeft;
  double distRight;
  double targetDistance;
  double currentDistance;
  double startYaw;
  boolean isGearLift;
  
  double[] ultrasonicValues;
  int ultrasonicValuesIndex;
  boolean ultrasonicValuesFilled;
  
  boolean aborted;
  
  long startTime;

  /**
   * Creates a new DriveUltrasonicCommand.
   * @param targetDistance The ultrasonic measurement to go to
   * @param isGearLift Whether to wiggle for gear lift or not
   */
  public DriveUltrasonicCommand(double targetDistance, boolean isGearLift) {
    super("DriveByUltrasonicCommand_" + targetDistance);
    this.targetDistance = targetDistance;
    this.isGearLift = isGearLift;
    requires(Robot.driveTrain);
  }
  
  public DriveUltrasonicCommand(double targetDistance) {
    this(targetDistance, false);
  }

  protected void initialize() {
    logger.info(String.format("Init, target=%f", targetDistance));

    startYaw = Robot.driveTrain.getYaw();
    
    aborted = false;
    ultrasonicValues = new double[25];
    ultrasonicValuesIndex = 0;
    ultrasonicValuesFilled = false;
    
    Robot.driveTrain.shift(DriveTrain.ShiftType.DOWN);
    startTime = System.nanoTime();
  }

  protected void execute() {
    double turn;
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
    
    if (isGearLift) {
//      yawDifference +=
//          5.5 * Math.sin((System.nanoTime() - startTime) * 2 * Math.PI / 1_000_000_000);
    }
    
    distLeft = Robot.proximitySensor.getDistanceLeft();
    distRight = Robot.proximitySensor.getDistanceRight();
    currentDistance = (distLeft + distRight) / 2;
    ultrasonicValues[ultrasonicValuesIndex++] = currentDistance;
    if (ultrasonicValuesIndex >= ultrasonicValues.length) {
      ultrasonicValuesFilled = true;
      ultrasonicValuesIndex = 0;
    }
    
    if (Math.abs(distLeft - targetDistance) < RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR ||
        Math.abs(distRight - targetDistance) < RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR) {
      turn = Math.signum(distRight - distLeft) * RobotMap.AUTO_TURN_MIN_SPEED_LOW;
    }
    
    turn = RobotMap.AUTO_DRIVE_TURN_P_LOW * yawDifference;
    if (turn > 1.0) {
      turn = 1.0;
    } else if (turn < -1.0) {
      turn = -1.0;
    }

    logger.debug(String.format("current=%f/%f, target=%f, yawError=%f", distLeft, distRight,
        targetDistance, yawDifference));

    double speed =
        RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + (Math.abs(currentDistance - targetDistance) * 0.1 / 5);
    if (speed > RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + 0.1) {
      speed = RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + 0.1;
    }
    if (currentDistance < targetDistance) {
      speed = 0;
    }
    Robot.driveTrain.rawThrottleTurnDrive(speed, turn);
  }

  @Override
  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      logger.warn("Aborted");
      return true;
    }
    
    if (ultrasonicValuesFilled) {
      double[] localCopy = new double[ultrasonicValues.length];
      System.arraycopy(ultrasonicValues, 0, localCopy, 0, ultrasonicValues.length);
      Arrays.sort(localCopy);
      double range = Math.abs(localCopy[0] - localCopy[localCopy.length - 1]);
      if (range < 2 && System.nanoTime() - startTime > 3_000_000_000L) {
        if (currentDistance < 14.8) {
          logger.info("Stopping distance isn't changing and <14.8in");
          return true;
        } else {
          aborted = true;
          logger.warn("Aborting because distance isn't changing");
          return true;
        }
      }
    }
    
    if (isGearLift && System.nanoTime() - startTime > 7_000_000_000L) {
      if (currentDistance < 14.8) {
        logger.warn("Stopping because timeout and < 14.8in");
      } else {
        aborted = true;
        logger.warn("Aborting because timeout");
      }
      return true;
    }

    return distLeft - RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR < targetDistance
        && distRight - RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR < targetDistance;
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
