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
  boolean toggle;
  boolean hold;

  /**
   * Creates a new GearCommand which toggles the current state of the gear.
   * Some people (@crf) say that toggling is not a useful interface, since the operator has
   * no way to tell what the current state of the system is...
   * 
   */
  public GearCommand() {
    toggle = true;
    hold = false;        
    logger.trace("Gear command in toggle mode");
  }
  
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
    this.toggle = false;
    this.shouldBeOpen = shouldBeOpen;
    this.hold = hold;
  }

  protected void initialize() {
    if (toggle) {
      logger.info(String.format("Initialize, toggle=%b, hold=%b", toggle, hold));
    } else {
      logger.info(String.format("Initialize, shouldBeOpen=%b, hold=%b", shouldBeOpen, hold));
    }
  }

  protected void execute() {
    if (toggle) {
      Robot.gearManipulator.setOpen(!Robot.gearManipulator.isOpen());
    } else  {
      Robot.gearManipulator.setOpen(shouldBeOpen);
    }
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
    logger.info("Interrupted");
    if (hold) {
      Robot.gearManipulator.setOpen(!shouldBeOpen);
    }
  }
}
