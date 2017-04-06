package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrainPID;
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
  
  double[] ultrasonicValues;
  int ultrasonicValuesIndex;
  boolean ultrasonicValuesFilled;
  
  boolean aborted;
  
  long startTime;
  
  boolean alignSquare;

  /**
   * Creates a new DriveUltrasonicCommand.
   * @param targetDistance The ultrasonic measurement to go to
   */
  public DriveUltrasonicCommand(double targetDistance, boolean alignSquare) {
    super("DriveByUltrasonicCommand_" + targetDistance);
    this.targetDistance = targetDistance;
    this.alignSquare = alignSquare;
    requires(Robot.driveTrain);
  }
  
  protected void initialize() {
    logger.info(String.format("Init, target=%f", targetDistance));

    startYaw = Robot.driveTrain.getYaw();
    
    aborted = false;
    ultrasonicValues = new double[25];
    ultrasonicValuesIndex = 0;
    ultrasonicValuesFilled = false;
    
    Robot.driveTrain.shift(DriveTrainPID.ShiftType.DOWN);
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
    
    distLeft = Robot.proximitySensor.getDistanceLeft();
    distRight = Robot.proximitySensor.getDistanceRight();
    currentDistance = (distLeft + distRight) / 2;
    ultrasonicValues[ultrasonicValuesIndex++] = currentDistance;
    if (ultrasonicValuesIndex >= ultrasonicValues.length) {
      ultrasonicValuesFilled = true;
      ultrasonicValuesIndex = 0;
    }
    
    if (alignSquare) {
      if (distLeft - targetDistance < 12.0 || distRight - targetDistance < 12.0) {
        turn = RobotMap.AUTO_DRIVE_ULTRASONIC_TURN_P * (distRight - distLeft);
      } else {
        turn = RobotMap.AUTO_DRIVE_TURN_P_LOW * yawDifference;
      }
    } else {
      turn = RobotMap.AUTO_DRIVE_TURN_P_LOW * yawDifference;
    }
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
      aborted = true;
      return true;
    }

    return currentDistance - RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR < targetDistance;
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
