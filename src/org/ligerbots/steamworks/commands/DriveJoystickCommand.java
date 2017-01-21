package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 * Command that lets you use the joysticks to drive.
 */
public class DriveJoystickCommand extends Command {

  public DriveJoystickCommand() {
    requires(Robot.driveTrain);
  }

  // Called just before this Command runs the first time
  protected void initialize() {}

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    Robot.driveTrain.joystickDrive(Robot.operatorInterface.getThrottle(),
        Robot.operatorInterface.getTurn());
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    return false;
  }

  // Called once after isFinished returns true
  protected void end() {}

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {
    Robot.driveTrain.joystickDrive(0, 0);
  }
}
