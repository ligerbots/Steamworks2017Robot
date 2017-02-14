package org.ligerbots.steamworks.commands;

import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command turns the robot by a certain number of degrees. Clockwise is positive (NavX
 * convention)
 */
public class TurnCommand extends AccessibleCommand {
  private static final Logger logger = LoggerFactory.getLogger(TurnCommand.class);

  double offsetDegrees;
  double maxTime; // seconds
  long startTime;
  double startingRotation;
  double targetRotation;
  boolean isClockwise;
  double error2;
  double error1;

  // did we end up where we want or was it aborted?
  boolean succeeded;
  boolean ended;

  /**
   * Create a new TurnCommand.
   * 
   * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
   */
  public TurnCommand(double offsetDegrees) {
    super("TurnCommand_" + offsetDegrees);
    requires(Robot.driveTrain);
    this.offsetDegrees = offsetDegrees;
    maxTime = 2.5 * (offsetDegrees / 180);
    // give at least 5 seconds for turning
    if (maxTime < 5) {
      maxTime = 5;
    }
    isClockwise = offsetDegrees > 0 && offsetDegrees <= 180;
  }

  // Called just before this Command runs the first time
  protected void initialize() {
    startTime = System.nanoTime();
    startingRotation = Robot.driveTrain.getYaw();
    targetRotation = DriveTrain.fixDegrees(startingRotation + offsetDegrees);
    logger.debug(String.format("Start %f, target %f", startingRotation, targetRotation));
    succeeded = false;
    ended = false;
  }

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    // We could be turning clockwise or counterclockwise, so an "error" of 350deg for example, is
    // actually an error of 10deg
    error1 = Math.abs(targetRotation - Robot.driveTrain.getYaw());
    error2 = Math.abs(360 - error1);
    double actualError = Math.min(error1, error2);
    if (actualError <= RobotMap.AUTO_TURN_RAMP_ZONE) {
      double driveSpeed = RobotMap.AUTO_TURN_MAX_SPEED * actualError / RobotMap.AUTO_TURN_RAMP_ZONE;
      if (driveSpeed < RobotMap.AUTO_TURN_MIN_SPEED) {
        driveSpeed = RobotMap.AUTO_TURN_MIN_SPEED;
      }
      Robot.driveTrain.rawThrottleTurnDrive(0, isClockwise ? -driveSpeed : driveSpeed);
    } else {
      Robot.driveTrain.rawThrottleTurnDrive(0,
          isClockwise ? -RobotMap.AUTO_TURN_MAX_SPEED : RobotMap.AUTO_TURN_MAX_SPEED);
    }
  }

  protected boolean isFinished() {
    boolean outOfTime = System.nanoTime() - startTime > (maxTime * RobotMap.NANOS_PER_SECOND);
    logger.debug(
        String.format("Error %f %f, absolute yaw %f", error1, error2, Robot.driveTrain.getYaw()));
    boolean onTarget = error1 < RobotMap.AUTO_TURN_ACCEPTABLE_ERROR
        || error2 < RobotMap.AUTO_TURN_ACCEPTABLE_ERROR;
    if (onTarget) {
      succeeded = true;
    }
    ended = outOfTime || onTarget || Robot.operatorInterface.isCancelled();
    return ended;
  }

  protected void end() {
    logger.info("Finish");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    ended = true;
  }

  protected void interrupted() {
    logger.warn("Interrupted");
    Robot.driveTrain.rawThrottleTurnDrive(0, 0);
    ended = true;
  }
  
  protected boolean isFailedToComplete() {
    return ended && !succeeded;
  }
}
