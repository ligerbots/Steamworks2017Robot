package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
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

  double targetDistance;
  double currentDistance;
  double startYaw;
  boolean wiggle;
  
  long startTime;

  /**
   * Creates a new DriveUltrasonicCommand.
   * @param targetDistance The ultrasonic measurement to go to
   * @param wiggle Whether to wiggle or not
   */
  public DriveUltrasonicCommand(double targetDistance, boolean wiggle) {
    super("DriveByUltrasonicCommand_" + targetDistance);
    this.targetDistance = targetDistance;
    this.wiggle = wiggle;
    requires(Robot.driveTrain);
  }
  
  public DriveUltrasonicCommand(double targetDistance) {
    this(targetDistance, false);
  }

  protected void initialize() {
    logger.info(String.format("Init, target=%f", targetDistance));

    startYaw = Robot.driveTrain.getYaw();
    
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
    
    if (wiggle) {
      yawDifference +=
          5 * Math.sin((System.nanoTime() - startTime) * 2 * Math.PI / 1_000_000_000);
    }
    
    turn = RobotMap.AUTO_DRIVE_TURN_P_LOW * yawDifference;
    if (turn > 1.0) {
      turn = 1.0;
    } else if (turn < -1.0) {
      turn = -1.0;
    }

    currentDistance = Robot.proximitySensor.getDistance();

    logger.debug(String.format("current=%f, target=%f, yawError=%f", currentDistance,
        targetDistance, yawDifference));

    double speed =
        RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + (Math.abs(currentDistance - targetDistance) * 0.1 / 5);
    if (speed > RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + 0.1) {
      speed = RobotMap.AUTO_DRIVE_MIN_SPEED_LOW + 0.1;
    }
    Robot.driveTrain.rawThrottleTurnDrive(currentDistance > targetDistance ? speed : -speed, turn);
  }

  @Override
  protected boolean isFinished() {
    if (Robot.operatorInterface.isCancelled()) {
      logger.warn("Aborted");
      return true;
    }

    return Math.abs(currentDistance - targetDistance) < RobotMap.AUTO_FINE_DRIVE_ACCEPTABLE_ERROR;
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
