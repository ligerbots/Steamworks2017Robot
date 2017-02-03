package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TurnCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(TurnCommand.class);

  double offsetDegrees;
  double maxTime; //seconds
  long startTime;
  double startingRotation;
  double targetRotation;
  boolean isClockwise;
  double error2;
  double error1;

  public TurnCommand(double offsetDegrees) {
    super("TurnCommand");
    requires(Robot.driveTrain);
    this.offsetDegrees = offsetDegrees;
    maxTime = 2.5 * (offsetDegrees / 180);
    isClockwise = offsetDegrees > 0 && offsetDegrees <= 180;
  }

  // Called just before this Command runs the first time
  protected void initialize() {
    startTime = System.nanoTime();
    startingRotation = Robot.driveTrain.getYaw();
    targetRotation = DriveTrain.fixDegrees(startingRotation + offsetDegrees);
    logger.debug(String.format("Start %f, target %f", startingRotation, targetRotation));
  }

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    error1 = Math.abs(targetRotation - Robot.driveTrain.getYaw());
    error2 = Math.abs(360 - error1);
    double actualError = Math.min(error1, error2);
    if (actualError <= 60) {
      double driveSpeed = 0.6 * actualError / 60;
      Robot.driveTrain.joystickDrive(0, isClockwise ? -driveSpeed : driveSpeed);
    } else {
      Robot.driveTrain.joystickDrive(0, isClockwise ? -0.6 : 0.6);
    }
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    boolean check1 = System.nanoTime() - startTime > (maxTime * RobotMap.NANOS_PER_SECOND);
    boolean check2;
    
    logger.debug(
        String.format("Error %f %f, absolute yaw %f", error1, error2, Robot.driveTrain.getYaw()));
    check2 = error1 < RobotMap.YAW_MARGIN || error2 < RobotMap.YAW_MARGIN;
    return check1 || check2 || Robot.operatorInterface.isCancelled();
  }

  // Called once after isFinished returns true
  protected void end() {}

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {}
  
}
