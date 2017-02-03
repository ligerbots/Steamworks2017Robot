package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.RobotMap;
import org.ligerbots.steamworks.subsystems.DriveTrain;

/**
 *
 */
public class TurnCommand extends Command {

  double degrees;
  double maxTime; //seconds
  long startTime;
  double startingRotation;
  double targetRotation;

  public TurnCommand(double degrees) {
    requires(Robot.driveTrain);
    this.degrees = degrees;
    maxTime = 2.5 * (degrees / 180);
    startingRotation = Robot.driveTrain.getYaw();
    targetRotation = DriveTrain.fixDegrees(startingRotation + degrees);
  }

  // Called just before this Command runs the first time
  protected void initialize() {
    startTime = System.nanoTime();
  }

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    Robot.driveTrain.joystickDrive(0, 1);
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    boolean check1 = System.nanoTime() - startTime > (maxTime * RobotMap.NANOS_PER_SECOND);
    boolean check2;
    double error1 = Math.abs(targetRotation - Robot.driveTrain.getYaw());
    double error2 = Math.abs(360 - error1);
    check2 = error1 < RobotMap.YAW_MARGIN || error2 < RobotMap.YAW_MARGIN;
    return check1 || check2 || Robot.operatorInterface.isCancelled();
  }

  // Called once after isFinished returns true
  protected void end() {}

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {}
  
  
}
