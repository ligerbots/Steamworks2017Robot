package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.OperatorInterface;
import org.ligerbots.steamworks.Robot;
import org.ligerbots.steamworks.subsystems.GearManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open or close the gear mechanism.
 */
public class GearHolderCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(GearHolderCommand.class);

  boolean open;
  boolean toggle;

  /**
   * Creates a new GearCommand which toggles the current state of the gear. Some people (@crf) say
   * that toggling is not a useful interface, since the operator has no way to tell what the current
   * state of the system is...
   * 
   */
  public GearHolderCommand() {
    toggle = true;
    logger.trace("Gear command in toggle mode");
  }

  /**
   * Creates a new GearCommand. The hold parameter is used in {@link OperatorInterface} for a
   * {@link JoystickButton#whileHeld(Command)} so the gear mechanism returns to closed position once
   * the button is released. This will eventually be replaced with autonomous control with auto
   * driving.
   * 
   * @param position Requested gear mech position
   */
  public GearHolderCommand(boolean open) {
    requires(Robot.gearManipulator);
    this.toggle = false;
    this.open = open;
  }

  protected void initialize() {
    if (toggle) {
      logger.info(String.format("Initialize, toggle=%b", toggle));
    } else {
      logger.info(String.format("Initialize, open=%b", open));
    }
  }

  protected void execute() {
    if (toggle) {
      Robot.gearManipulator.setGearHolder(!Robot.gearManipulator.getGearHolder());
    } else {
      Robot.gearManipulator.setGearHolder(open);
    }
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.trace("Finish");
  }

  protected void interrupted() {
    logger.info("Interrupted");
  }
}
