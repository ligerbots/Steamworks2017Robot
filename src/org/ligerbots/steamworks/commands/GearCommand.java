package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 * Open or close the gear mechanism.
 */
public class GearCommand extends Command {

  boolean isOpen;
  
  /**
   * Update the value of isOpen.
   * @param isOpen True when the ear mechanism is open
   */
  public GearCommand(boolean isOpen) {
    requires(Robot.gearManipulator);
    this.isOpen = isOpen;
  }

  // Called just before this Command runs the first time
  protected void initialize() {}

  protected void execute() {
    if (isOpen) {
      Robot.gearManipulator.openManipulator();
    } else {
      Robot.gearManipulator.closeManipulator();
    }
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
