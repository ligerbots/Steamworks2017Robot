package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open or close the gear mechanism.
 */
public class GearCommand extends Command {

  boolean shouldBeOpen;
  boolean hold;
  
  /**
   * Creates a new GearCommand.
   * @param shouldBeOpen Whether the gear mechanism should be open or not.
   */

  Logger logger = LoggerFactory.getLogger(GearCommand.class);
  public GearCommand(boolean shouldBeOpen) {
    this(shouldBeOpen, false);
    logger.trace("Gear command: Setting shouldBeOpen to " + shouldBeOpen);
  }
  
  public GearCommand(boolean shouldBeOpen, boolean hold) {
    requires(Robot.gearManipulator);
    this.shouldBeOpen = shouldBeOpen;
    this.hold = hold;
    logger.trace("Gear command: Setting shouldBeOpen to " + shouldBeOpen + " and hold to " + hold);
  }

  protected void initialize() {}

  protected void execute() {
    Robot.gearManipulator.setOpen(shouldBeOpen);
  }

  protected boolean isFinished() {
    return !hold;
  }

  protected void end() {
    if (hold) {
      Robot.gearManipulator.setOpen(!shouldBeOpen);
    }
  }

  protected void interrupted() {
    if (hold) {
      Robot.gearManipulator.setOpen(!shouldBeOpen);
    }
  }
}
