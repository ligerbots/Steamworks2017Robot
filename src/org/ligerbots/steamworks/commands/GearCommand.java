package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;

/**
 * Open or close the gear mechanism.
 */
public class GearCommand extends Command {

  boolean shouldBeOpen;
  
  /**
   * Creates a new GearCommand.
   * @param shouldBeOpen Whether the gear mechanism should be open or not.
   */
  public GearCommand(boolean shouldBeOpen) {
    requires(Robot.gearManipulator);
    this.shouldBeOpen = shouldBeOpen;
  }

  protected void initialize() {}

  protected void execute() {
    Robot.gearManipulator.setOpen(shouldBeOpen);
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {}

  protected void interrupted() {}
}
