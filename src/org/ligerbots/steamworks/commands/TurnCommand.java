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
  
  boolean isHighGear;
  
  double autoTurnRampZone;
  double autoTurnMaxSpeed;
  double autoTurnMinSpeed;
  double acceptableError;
  
  String turnZones[] = {"RAMPZONE", "MINSPEED", "MAXSPEED",};

  /**
   * Create a new TurnCommand.
   * 
   * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
   * @param acceptableError How many degrees off the turn is allowed to be. 
   */
  
  public TurnCommand(double offsetDegrees, double acceptableError) {
	    super("TurnCommand_" + offsetDegrees + "_" + acceptableError);
	    requires(Robot.driveTrain);
	    SetParameters(offsetDegrees, acceptableError);
	  }

  /**
   * Create a new TurnCommand.
   * 
   * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
   */
public TurnCommand(double offsetDegrees) {
    super("TurnCommand_" + offsetDegrees + "_" + RobotMap.AUTO_TURN_ACCEPTABLE_ERROR);
    requires(Robot.driveTrain);
    SetParameters(offsetDegrees,  RobotMap.AUTO_TURN_ACCEPTABLE_ERROR);
  }

/**
 * Set parameters for turn command
 * 
 * @param offsetDegrees The number of degrees to turn by. Clockwise is positive
 * @param acceptableError How many degrees off the turn is allowed to be. 
 */

public void SetParameters(double offsetDegrees, double acceptableError) {
    this.acceptableError = acceptableError;
    offsetDegrees = DriveTrain.fixDegrees(offsetDegrees);
    if (offsetDegrees > 180) {
      offsetDegrees = offsetDegrees - 360;
    }
    this.offsetDegrees = offsetDegrees;
    maxTime = 2.5 * (Math.abs(offsetDegrees) / 180);
    // give at least 5 seconds for turning
    if (maxTime < 5) {
      maxTime = 5;
    }
    isClockwise = offsetDegrees > 0;
  }
  
  // Called just before this Command runs the first time
  protected void initialize() {
    startTime = System.nanoTime();
    startingRotation = DriveTrain.fixDegrees(Robot.driveTrain.getYaw());
    targetRotation = DriveTrain.fixDegrees(startingRotation + offsetDegrees);
    logger.debug(String.format("Start %f, target %f", startingRotation, targetRotation));
    
    succeeded = false;
    ended = false;
    Robot.driveTrain.shift(DriveTrain.ShiftType.DOWN);
    isHighGear = false;
    
    if (isHighGear) {
      autoTurnRampZone = RobotMap.AUTO_TURN_RAMP_ZONE_HIGH;
      autoTurnMaxSpeed = RobotMap.AUTO_TURN_MAX_SPEED_HIGH;
      autoTurnMinSpeed = RobotMap.AUTO_TURN_MIN_SPEED_HIGH;
    } else {
      autoTurnRampZone = RobotMap.AUTO_TURN_RAMP_ZONE_LOW;
      autoTurnMaxSpeed = RobotMap.AUTO_TURN_MAX_SPEED_LOW;
      autoTurnMinSpeed = RobotMap.AUTO_TURN_MIN_SPEED_LOW;
    }
  }
  
  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    double localTargetRotation = targetRotation;
    double currentRotation = DriveTrain.fixDegrees(Robot.driveTrain.getYaw());
    
    if (localTargetRotation < currentRotation) {
      localTargetRotation += 360;
    }

    isClockwise = localTargetRotation - currentRotation <= 180;
    
    // We could be turning clockwise or counterclockwise, so an "error" of 350deg for example, is
    // actually an error of 10deg
    error1 = Math.abs(targetRotation - currentRotation);
    error2 = Math.abs(360 - error1);
    double actualError = Math.min(error1, error2);
    double driveSpeed;
    int zone;
    if (actualError <= autoTurnRampZone) {
      zone = 0;
      driveSpeed = autoTurnMaxSpeed * actualError / autoTurnRampZone;
      if (Math.abs(driveSpeed) < autoTurnMinSpeed) {
        zone = 1;
        driveSpeed = autoTurnMinSpeed;
      }
    } else {
      zone = 2;
      driveSpeed = autoTurnMaxSpeed;
    }
    driveSpeed = isClockwise ? -driveSpeed : driveSpeed;
    Robot.driveTrain.rawThrottleTurnDrive(0, driveSpeed);  
    logger.debug(String.format("Zone %s, drivespeed %5.3f", turnZones[zone], driveSpeed));
  }

  protected boolean isFinished() {
    boolean outOfTime = System.nanoTime() - startTime > (maxTime * RobotMap.NANOS_PER_SECOND);
    logger.debug(
        String.format("Error %f %f, absolute yaw %f", error1, error2, Robot.driveTrain.getYaw()));
    boolean onTarget = error1 < acceptableError
        || error2 < acceptableError;
    if (onTarget) {
      succeeded = true;
    }
    ended = outOfTime || onTarget || Robot.operatorInterface.isCancelled();
    return ended;
  }

  protected void end() {
    logger.info(String.format("Finished due to %s, final error = %f, acceptableError = %5.2f in %5.2f seconds",
        succeeded ? "Succeded" : (Robot.operatorInterface.isCancelled() ?  "Cancelled" : "Timeout"),                  
        error1, acceptableError, (System.nanoTime() - startTime) / RobotMap.NANOS_PER_SECOND)); 
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
