package org.ligerbots.steamworks.commands;

import edu.wpi.first.wpilibj.command.Command;
import org.ligerbots.steamworks.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command toggles the intake on and off.
 */
public class IntakeCommand extends Command {
  private static final Logger logger = LoggerFactory.getLogger(IntakeCommand.class);

  boolean setIntakeOn;
  boolean toggle;

  /**
   * Creates an IntakeCommand that turns the intake on or off.
   * @param setIntakeOn true for on
   */
  public IntakeCommand(boolean setIntakeOn) {
    requires(Robot.intake);
    this.setIntakeOn = setIntakeOn;
    this.toggle = false;
  }

  /**
   * Creates an IntakeCommand that toggles the intake.
   */
  public IntakeCommand() {
    requires(Robot.intake);
    toggle = true;
  }

  protected void initialize() {
    logger.info(String.format("Initialize, set intake=%b", setIntakeOn));
  }

  protected void execute() {
    if (toggle) {
      Robot.intake.setIntakeOn(!Robot.intake.isIntakeOn());
    } else {
      Robot.intake.setIntakeOn(setIntakeOn);
    }
  }

  protected boolean isFinished() {
    return true;
  }

  protected void end() {
    logger.info("Finish");
  }

  protected void interrupted() {
    logger.warn("Interrupted");
  }
}
