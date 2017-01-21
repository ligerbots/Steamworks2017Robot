package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 *
 */
public class ShiftCommand extends Command {

  boolean shiftUp;
  
  public ShiftCommand(boolean shiftUp) {
    this.shiftUp = shiftUp;
  }

  // Called just before this Command runs the first time
  protected void initialize() {}

  // Called repeatedly when this Command is scheduled to run
  protected void execute() {
    Robot.driveTrain.shift(shiftUp);
  }

  // Make this return true when this Command no longer needs to run execute()
  protected boolean isFinished() {
    return true;
  }

  // Called once after isFinished returns true
  protected void end() {}

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  protected void interrupted() {}
}
