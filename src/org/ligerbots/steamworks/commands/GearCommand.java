package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.OperatorInterface;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open or close the gear mechanism.
 */
public class GearCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(GearCommand.class);

  boolean shouldBeOpen;
  boolean hold;

  /**
   * Creates a new GearCommand.
   * 
   * @param shouldBeOpen Whether the gear mechanism should be open or not.
   */
  public GearCommand(boolean shouldBeOpen) {
    this(shouldBeOpen, false);
    logger.trace("Gear command: Setting shouldBeOpen to " + shouldBeOpen);
  }

  /**
   * Creates a new GearCommand. The hold parameter is used in {@link OperatorInterface} for a
   * {@link JoystickButton#whileHeld(Command)} so the gear mechanism returns to closed position once
   * the button is released. This will eventually be replaced with autonomous control with auto
   * driving.
   * 
   * @param shouldBeOpen Whether the gear mechanism should be open or not.
   * @param hold Whether this command should keep running until canceled or not.
   */
  public GearCommand(boolean shouldBeOpen, boolean hold) {
    requires(Robot.gearManipulator);
    this.shouldBeOpen = shouldBeOpen;
    this.hold = hold;
  }

  protected void initialize() {
    logger.info("Initialize, shouldBeOpen=%b, hold=%b", shouldBeOpen, hold);
  }

  protected void execute() {
    Robot.gearManipulator.setOpen(shouldBeOpen);
  }

  protected boolean isFinished() {
    return !hold;
  }

  protected void end() {
    logger.trace("Finish");
    if (hold) {
      Robot.gearManipulator.setOpen(!shouldBeOpen);
    }
  }

  protected void interrupted() {
    logger.warn("Interrupted");
    if (hold) {
      Robot.gearManipulator.setOpen(!shouldBeOpen);
    }
  }
}
