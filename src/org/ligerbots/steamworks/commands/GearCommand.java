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
public class GearCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(GearCommand.class);

  GearManipulator.Position position;
  boolean toggle;

  /**
   * Creates a new GearCommand which toggles the current state of the gear. Some people (@crf) say
   * that toggling is not a useful interface, since the operator has no way to tell what the current
   * state of the system is...
   * 
   */
  public GearCommand() {
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
  public GearCommand(GearManipulator.Position position) {
    requires(Robot.gearManipulator);
    this.toggle = false;
    this.position = position;
  }

  protected void initialize() {
    if (toggle) {
      logger.info(String.format("Initialize, toggle=%b", toggle));
    } else {
      logger.info(String.format("Initialize, position=%s", position.toString()));
    }
  }

  protected void execute() {
    if (toggle) {
      Robot.gearManipulator
          .setPosition(Robot.gearManipulator.getPosition() == GearManipulator.Position.CLOSED
              ? GearManipulator.Position.DELIVER_GEAR : GearManipulator.Position.CLOSED);
    } else if (position == GearManipulator.Position.DELIVER_GEAR) {
      Robot.gearManipulator.setGearHolder(true);
      WaitCommand waitCommand = new WaitCommand(1000000000);
      Robot.gearManipulator.setPosition(position);
    }
    else {
      Robot.gearManipulator.setPosition(position);
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
